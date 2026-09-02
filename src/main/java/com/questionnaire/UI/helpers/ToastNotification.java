package com.questionnaire.UI.helpers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.questionnaire.UI.enums.ToastType;

public class ToastNotification {
    private final JFrame parentFrame;

    // Track active windows globally to handle stacking offset
    private static final List<ToastInstance> activeToasts = new ArrayList<>();
    private static final int TOAST_GAP = 8;
    private static final int TOP_OFFSET = 50;

    // Helper holder for active window & its parent
    private static class ToastInstance {
        final JWindow window;
        final JFrame parent;

        ToastInstance(JWindow window, JFrame parent) {
            this.window = window;
            this.parent = parent;
        }
    }

    public ToastNotification(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void showError(String message) {
        this.show(message, ToastType.ERROR);
    }

    public void showSuccess(String message) {
        this.show(message, ToastType.SUCCESS);
    }

    public void showWarning(String message) {
        this.show(message, ToastType.WARNING);
    }

    public void showInfo(String message) {
        this.show(message, ToastType.INFO);
    }

    public void show(String message, ToastType type) {
        SwingUtilities.invokeLater(() -> {
            JWindow toast = new JWindow(this.parentFrame);
            toast.setType(JWindow.Type.POPUP);

            // Container Panel with rounded look and color themes
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBackground(type.getBgColor());
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(type.getBorderColor(), 2, true),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)));

            JLabel iconLabel = new JLabel(type.getIcon());
            iconLabel.setFont(iconLabel.getFont().deriveFont(16f));

            JLabel textLabel = new JLabel("<html><b>" + message + "</b></html>");
            textLabel.setFont(textLabel.getFont().deriveFont(Font.PLAIN, 13f));
            textLabel.setForeground(Color.DARK_GRAY);

            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(textLabel, BorderLayout.CENTER);
            toast.add(panel);

            toast.pack();

            ToastInstance instance = new ToastInstance(toast, this.parentFrame);
            activeToasts.add(instance);

            repositionToasts(this.parentFrame);
            toast.setVisible(true);

            // Auto-close timer
            Timer timer = new Timer(3000, e -> dismissToast(instance));
            timer.setRepeats(false);
            timer.start();
        });
    }

    private static void dismissToast(ToastInstance instance) {
        instance.window.setVisible(false);
        instance.window.dispose();
        activeToasts.remove(instance);
        repositionToasts(instance.parent);
    }

    private static void repositionToasts(JFrame parentFrame) {
        if (parentFrame == null || !parentFrame.isVisible())
            return;

        int startX = parentFrame.getX() + parentFrame.getWidth() - 20;
        int currentY = parentFrame.getY() + TOP_OFFSET;

        for (ToastInstance item : activeToasts) {
            if (item.parent == parentFrame) {
                int x = startX - item.window.getWidth();
                item.window.setLocation(x, currentY);
                currentY += item.window.getHeight() + TOAST_GAP;
            }
        }
    }
}