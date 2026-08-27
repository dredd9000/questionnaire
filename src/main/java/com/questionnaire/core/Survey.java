package com.questionnaire.core;

import java.util.List;
import java.util.Set;

import com.questionnaire.core.enums.ESurveyStatus;
import com.questionnaire.core.model.Client;
import com.questionnaire.core.model.Question;

import lombok.Data;

@Data
public class Survey {
    private List<Question> questions;
    private Set<Client> group;
    private ESurveyStatus status;

    public Survey(List<Question> questions, Set<Client> group) {
        this.questions = questions;
        this.group = group;
        this.status = ESurveyStatus.PRE;
    }

    public void startSurvey() {
        this.status = ESurveyStatus.STARTED;
    }

    public void endSurvey() {
        this.status = ESurveyStatus.ENDED;
    }
}
