package com.questionnaire.UI.model;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;

@Data
public class RowValueData {
    private int rowIdx;
    private AtomicInteger answeredCount;

    public RowValueData(int rowIdx) {
        this.rowIdx = rowIdx;

        this.answeredCount = new AtomicInteger(0);
    }

    public int getAnsweredCount() {
        return this.answeredCount.get();
    }

    public void incAnsweredCount() {
        this.answeredCount.incrementAndGet();
    }
}
