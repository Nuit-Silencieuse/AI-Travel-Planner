package com.example.aitravelplanner.planner;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.example.aitravelplanner.user.User;
import com.example.aitravelplanner.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;
    private final String aliyunApiKey;

    public TravelPlanService(TravelPlanRepository travelPlanRepository,
                             UserRepository userRepository,
                             @Value("${aliyun.llm.api-key}") String aliyunApiKey) {
        this.travelPlanRepository = travelPlanRepository;
        this.userRepository = userRepository;
        this.aliyunApiKey = aliyunApiKey;
    }

    public TravelPlan generateTravelPlan(String destination, LocalDate startDate, LocalDate endDate, Double budget, String preferences) {
        // Get current authenticated user's email from JWT
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = jwt.getClaimAsString("email");

        // Find or create user in our database
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(userEmail);
                    newUser.setUsername(userEmail); // Use email as username for simplicity
                    return userRepository.save(newUser);
                });

        String userPrompt = buildPrompt(destination, startDate, endDate, budget, preferences);

        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("You are a helpful travel planning assistant. For each activity, you must provide its location coordinates. Always return the plan in a valid, complete JSON format.")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(userPrompt)
                    .build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(aliyunApiKey)
                    .model("qwen-plus") // Ensure this model is available for your key
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            GenerationResult result = gen.call(param);
            String planDetailsJson = result.getOutput().getChoices().get(0).getMessage().getContent();

            TravelPlan travelPlan = new TravelPlan();
            travelPlan.setUserId(currentUser.getId());
            travelPlan.setTitle("Travel Plan for " + destination);
            travelPlan.setDestination(destination);
            travelPlan.setStartDate(startDate);
            travelPlan.setEndDate(endDate);
            travelPlan.setBudget(budget);
            travelPlan.setPreferences(preferences);
            travelPlan.setPlanDetails(planDetailsJson);

            return travelPlanRepository.save(travelPlan);

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            // In a real application, you'd want more sophisticated error handling
            throw new RuntimeException("Error calling LLM service: " + e.getMessage(), e);
        }
    }

    public List<TravelPlan> getUserTravelPlans() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = jwt.getClaimAsString("email");

        Optional<User> currentUser = userRepository.findByEmail(userEmail);
        return currentUser.map(user -> travelPlanRepository.findByUserId(user.getId())).orElse(List.of());
    }

    private String buildPrompt(String destination, LocalDate startDate, LocalDate endDate, Double budget, String preferences) {
        // Construct a detailed prompt for the LLM
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请为我规划一个旅行计划。\n");
        promptBuilder.append("目的地: ").append(destination).append("\n");
        if (startDate != null && endDate != null) {
            promptBuilder.append("日期: 从 ").append(startDate).append(" 到 ").append(endDate).append("\n");
        }
        if (budget != null) {
            promptBuilder.append("预算: ").append(budget).append("元\n");
        }
        if (preferences != null && !preferences.isEmpty()) {
            promptBuilder.append("偏好: ").append(preferences).append("\n");
        }
        promptBuilder.append("请以 JSON 格式返回行程数据。对于每一个活动(activity)，如果它有具体的地点，请务必包含一个 location 对象，其中包含 lng (经度) 和 lat (纬度) 字段。");
        promptBuilder.append("例如: {\"days\": [{\"day\": 1, \"activities\": [{\"time\": \"9:00\", \"description\": \"参观外滩\", \"location\": {\"lng\": 121.4913, \"lat\": 31.2392}}]}]}\n");
        promptBuilder.append("确保 JSON 格式正确且完整，不要在 JSON 内容之外添加任何解释性文字。");
        return promptBuilder.toString();
    }
}
