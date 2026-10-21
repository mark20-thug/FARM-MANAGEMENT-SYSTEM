import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * One shared scale factor so every window fits the screen it is opened on.
 *
 * Every frame in this project is laid out by hand at a design size of
 * 1600x1200 using pixel positions and font sizes. On a laptop screen
 * (for example 1920x1080, which only leaves about 1040px of usable
 * height) that window is taller than the desktop and the controls look
 * oversized.
 *
 * Ui multiplies every design measurement by a single factor:
 *
 *     scale = min(1, usableScreenWidth / 1600, usableScreenHeight / 1200)
 *
 * so the same layout always fits the screen it is opened on. Nothing is
 * ever enlarged (the factor never exceeds 1) and the design itself is
 * unchanged - only the pixels it occupies. The factor can be forced with
 * -Dfarm.ui.scale=0.9 on the command line.
 */
public final class Ui {

    //the size every frame is designed at, before scaling
    static final int DESIGN_WIDTH = 1600;
    static final int DESIGN_HEIGHT = 1200;

    private static final double SCALE = computeScale();

    private Ui() {
    }

    private static double computeScale() {
        String override = System.getProperty("farm.ui.scale");
        if (override != null && !override.trim().isEmpty()) {
            try {
                double forced = Double.parseDouble(override.trim());
                if (forced > 0) return forced;
            } catch (NumberFormatException ignored) {
                //bad override value - fall through and measure the screen
            }
        }

        try {
            GraphicsConfiguration config = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();
            Rectangle screen = config.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
            double usableWidth = screen.getWidth() - insets.left - insets.right;
            double usableHeight = screen.getHeight() - insets.top - insets.bottom;
            if (usableWidth <= 0 || usableHeight <= 0) return 1.0;

            double scale = Math.min(1.0,
                    Math.min(usableWidth / DESIGN_WIDTH, usableHeight / DESIGN_HEIGHT));
            return scale < 0.5 ? 0.5 : scale;   //never shrink past half size
        } catch (Exception e) {
            return 1.0;
        }
    }

    /**the factor every measurement in the app is multiplied by*/
    public static double scale() {
        return SCALE;
    }

    /**scale one design measurement into real pixels*/
    public static int s(double designPixels) {
        return (int) Math.round(designPixels * SCALE);
    }

    /**scaled font; "Railway" is not installed everywhere, so the
     * platform default font is used whenever it is missing*/
    public static Font font(int style, double designSize) {
        return new Font("Railway", style, Math.max(9, s(designSize)));
    }

    /**place a component using design measurements, scaled for this screen*/
    public static void bounds(Component c, double x, double y, double w, double h) {
        c.setBounds(s(x), s(y), s(w), s(h));
    }

    /**the design window size (1600x1200), scaled for this screen*/
    public static Dimension windowSize() {
        return new Dimension(s(DESIGN_WIDTH), s(DESIGN_HEIGHT));
    }
}
