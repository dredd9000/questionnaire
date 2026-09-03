package com.questionnaire.UI.enums;

import java.awt.Color;

import lombok.Getter;

@Getter
public enum ToastType {
    SUCCESS(new Color(40, 167, 69), new Color(230, 245, 233), "✅"),
    WARNING(new Color(255, 193, 7), new Color(255, 248, 225), "⚠️"),
    ERROR(new Color(220, 53, 69), new Color(253, 237, 239), "❌"),
    INFO(new Color(23, 162, 184), new Color(227, 242, 253), "✨");

    final Color borderColor;
    final Color bgColor;
    final String icon;

    ToastType(Color borderColor, Color bgColor, String icon) {
        this.borderColor = borderColor;
        this.bgColor = bgColor;
        this.icon = icon;
    }
}
