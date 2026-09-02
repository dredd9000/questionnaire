package com.questionnaire.UI.enums;

import lombok.Getter;

public enum PollStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    @Getter
    private final String label;

    private PollStatus(String label) {
        this.label = label;
    }
}
