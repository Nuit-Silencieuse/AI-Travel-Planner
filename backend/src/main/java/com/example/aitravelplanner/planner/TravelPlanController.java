package com.example.aitravelplanner.planner;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    public TravelPlanController(TravelPlanService travelPlanService) {
        this.travelPlanService = travelPlanService;
    }

    @PostMapping("/generate")
    public TravelPlan generatePlan(@RequestBody PlanRequest request) {
        return travelPlanService.generateTravelPlan(
                request.getDestination(),
                request.getStartDate(),
                request.getEndDate(),
                request.getBudget(),
                request.getPreferences()
        );
    }

    @GetMapping
    public List<TravelPlan> getUserPlans() {
        return travelPlanService.getUserTravelPlans();
    }
}
