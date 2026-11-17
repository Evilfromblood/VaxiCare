package com.vaxicare.ui;

import com.vaxicare.model.Vaccine;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Custom Pure AWT Canvas component visually rendering vaccine vial capacity and shelf-life.
 * Demonstrates Unit IV: java.awt.Canvas custom component painting and graphics primitives.
 */
public class VialCanvas extends Canvas {

    private int dosesRemaining = 0;
    private int maxDoses = 10;
    private int vialNumber = 0;
    private long remainingShelfLifeMinutes = 0;
    private long elapsedMinutes = 0;
    private int dosesWasted = 0;

    public VialCanvas() {
        setPreferredSize(new Dimension(520, 85));
        setBackground(new Color(244, 241, 234)); // Soft cream
    }

    public synchronized void updateState(int remaining, int max, int vialNum,
                                        long remainingMins, long elapsedMins, int wasted) {
        this.dosesRemaining = Math.max(0, remaining);
        this.maxDoses = (max > 0) ? max : 10;
        this.vialNumber = vialNum;
        this.remainingShelfLifeMinutes = remainingMins;
        this.elapsedMinutes = elapsedMins;
        this.dosesWasted = wasted;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        update(g);
    }

    @Override
    public void update(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Background fill
        g2.setColor(new Color(244, 241, 234));
        g2.fillRect(0, 0, width, height);

        // 2. Container card box
        g2.setColor(new Color(234, 230, 220));
        g2.fillRoundRect(4, 4, width - 8, height - 8, 8, 8);
        g2.setColor(new Color(209, 199, 189));
        g2.drawRoundRect(4, 4, width - 8, height - 8, 8, 8);

        // 3. Header text: Vial # & Status
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(new Color(36, 43, 61)); // Deep Navy
        String title = "Physical Vial Lifecycle Monitor | Current: Vial #" + vialNumber;
        g2.drawString(title, 14, 22);

        String status;
        Color statusColor;
        if (dosesRemaining > 0 && remainingShelfLifeMinutes > 0) {
            status = "● ACTIVE (" + remainingShelfLifeMinutes / 60 + "h " + (remainingShelfLifeMinutes % 60) + "m left)";
            statusColor = new Color(22, 163, 74); // Green
        } else if (dosesRemaining > 0 && remainingShelfLifeMinutes == 0) {
            status = "● EXPIRED (>6h Limit)";
            statusColor = new Color(220, 38, 38); // Red
        } else if (vialNumber > 0) {
            status = "● EXHAUSTED (0 doses)";
            statusColor = new Color(217, 119, 6); // Amber
        } else {
            status = "● NO VIAL OPENED";
            statusColor = new Color(100, 116, 139); // Slate
        }

        g2.setColor(statusColor);
        FontMetrics fm = g2.getFontMetrics();
        int statusWidth = fm.stringWidth(status);
        g2.drawString(status, width - statusWidth - 14, 22);

        // 4. Progress Bar track
        int barX = 14;
        int barY = 32;
        int barW = width - 28;
        int barH = 24;

        g2.setColor(new Color(215, 210, 200));
        g2.fillRoundRect(barX, barY, barW, barH, 6, 6);
        g2.setColor(new Color(180, 175, 165));
        g2.drawRoundRect(barX, barY, barW, barH, 6, 6);

        // 5. Fill progress based on dosesRemaining / maxDoses
        double pct = (double) dosesRemaining / (double) maxDoses;
        if (pct > 1.0) pct = 1.0;
        int fillW = (int) (barW * pct);

        if (fillW > 0) {
            Color fillColor;
            if (dosesRemaining >= 6) {
                fillColor = new Color(30, 111, 120); // Teal
            } else if (dosesRemaining >= 3) {
                fillColor = new Color(217, 119, 6); // Amber
            } else {
                fillColor = new Color(220, 38, 38); // Crimson
            }
            g2.setColor(fillColor);
            g2.fillRoundRect(barX + 1, barY + 1, fillW - 2, barH - 2, 5, 5);
        }

        // 6. Text inside progress bar
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(fillW > (barW / 2) ? Color.WHITE : new Color(36, 43, 61));
        String barText = String.format("Doses Remaining in Active Vial: %d / %d  (%d%% Capacity)",
                dosesRemaining, maxDoses, (int)(pct * 100));
        FontMetrics bfm = g2.getFontMetrics();
        int bTextX = barX + (barW - bfm.stringWidth(barText)) / 2;
        int bTextY = barY + 16;
        g2.drawString(barText, bTextX, bTextY);

        // 7. Footer sub-labels: Wasted & Expiry limits
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(75, 85, 99));
        String subText = String.format("Open Shelf-Life Limit: %d Hours | Cumulative Doses Wasted: %d | Total Vials Opened: %d",
                Vaccine.VIAL_OPEN_HOURS, dosesWasted, vialNumber);
        g2.drawString(subText, 14, height - 12);
    }
}
