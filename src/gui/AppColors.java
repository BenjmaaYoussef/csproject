package gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Shared color palette and fonts for the Workout Tracker GUI.
 */
public class AppColors {

    // Background
    public static final Color BG           = new Color(0xF0F2F5);
    public static final Color CARD         = Color.WHITE;

    // Brand
    public static final Color PRIMARY      = new Color(0x4361EE);
    public static final Color PRIMARY_DARK = new Color(0x3451D1);

    // Semantic
    public static final Color SUCCESS      = new Color(0x06D6A0);
    public static final Color WARNING      = new Color(0xFFD166);
    public static final Color DANGER       = new Color(0xEF476F);

    // Type colours
    public static final Color STRENGTH     = new Color(0x4361EE);
    public static final Color CARDIO       = new Color(0x06D6A0);
    public static final Color FLEXIBILITY  = new Color(0xFFD166);

    // Text
    public static final Color TEXT_DARK    = new Color(0x2B2D42);
    public static final Color TEXT_MID     = new Color(0x555770);
    public static final Color TEXT_LIGHT   = new Color(0x8D99AE);

    // Fonts
    public static final Font  FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font  FONT_HEADING = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font  FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font  FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font  FONT_STAT    = new Font("Segoe UI", Font.BOLD,  28);

    private AppColors() {}
}
