package com.example.aitravelplanner.planner;

import com.example.aitravelplanner.user.User;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "travel_plans")
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String destination;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private Double budget;

    private String preferences;

    @Type(JsonType.class)
    @Column(name = "plan_details", columnDefinition = "jsonb")
    private String planDetails; // Store LLM generated JSON as String

    // Getters and setters

}
