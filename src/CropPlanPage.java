import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CropPlanPage extends JFrame {

    String userName;
    String crop;
    double acres;
    Dashboard back;

    static class CropInfo {
        String variety, planting, harvest, rotation, irrigation, pests;
        double seedRate;      // kg of seed per acre
        double n, p, k;       // kg per acre of N, P2O5, K2O
        double yieldPerAcre;  // tonnes per acre

        CropInfo(String variety, String planting, String harvest, String rotation,
                 String irrigation, String pests, double seedRate,
                 double n, double p, double k, double yieldPerAcre) {
            this.variety = variety;
            this.planting = planting;
            this.harvest = harvest;
            this.rotation = rotation;
            this.irrigation = irrigation;
            this.pests = pests;
            this.seedRate = seedRate;
            this.n = n;
            this.p = p;
            this.k = k;
            this.yieldPerAcre = yieldPerAcre;
        }
    }

    static final Map<String, CropInfo> CROPS = new HashMap<>();

    static {
        CROPS.put("Wheat", new CropInfo(
                "HD-2967 / HB-2032 (bread wheat)",
                "Oct-Nov, within the first 2 weeks of the rains",
                "Feb-Mar, about 120-140 days after sowing",
                "Rotate with soybean or another legume every 3rd season to break rust and soil-borne carryover; follow with early land preparation.",
                "Irrigate at crown-root, tillering and booting stages (3-4 irrigations); keep soil at 60-70% field capacity and avoid waterlogging.",
                "Scout weekly for aphids, rust and powdery mildew; treat at first sign and rotate chemistry.",
                45, 48, 24, 16, 1.4));

        CROPS.put("Rice", new CropInfo(
                "NERICA 4 / IR-8 (lowland)",
                "Jun-Jul transplanting; raise the seedbed 25-30 days earlier",
                "Oct-Nov, about 150-160 days after transplanting",
                "Alternate with soybean or groundnuts in the dry season to break pest cycles and add nitrogen to the soil.",
                "Hold 5-7 cm shallow flooding from tillering to flowering; drain 7-10 days before harvest.",
                "Watch for stem borer, blast and rice weevil; use resistant varieties and pheromone traps.",
                30, 40, 20, 20, 1.8));

        CROPS.put("Soybean", new CropInfo(
                "Pandakoma I / NS-708",
                "Mar-Apr or Aug-Sep, within 2 weeks of the rains",
                "Jul-Aug or Dec-Jan, about 90-110 days",
                "Best after maize or wheat - fixes 40-60 kg nitrogen per acre for the next crop; never follow another legume.",
                "Critical at flowering and pod fill (2 irrigations); sensitive to waterlogging at emergence, so ensure drainage.",
                "Scout for pod borer, aphids and soybean rust; start control at 5-10% pod damage.",
                25, 0, 25, 15, 1.1));

        CROPS.put("Sugarcane", new CropInfo(
                "NCo-334 / Co-419",
                "Feb-Apr, on ridges with trash mulch",
                "Dec-Mar, 12-18 months after planting",
                "Ratoon 2-3 crops then rotate with soybean or millet; intercrop with beans or pumpkin during the first 4 months.",
                "Water heavily at tillering and grand growth (Jun-Sep); mulch to hold moisture and reduce 6-8 weeks before harvest to raise sucrose.",
                "Monitor for stem borer, smut and whitefly; destroy infested stools and plant only clean seed cane.",
                2500, 60, 30, 40, 28));
    }

    public CropPlanPage(String userName, String crop, double acres, Dashboard back) {
        this.userName = userName;
        this.crop = crop;
        this.acres = acres;
        this.back = back;

        setLayout(null);
        setTitle("Farm_Management_System");
        setSize(1600, 1200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CropInfo info = CROPS.get(crop);
        if (info == null) info = CROPS.get("Wheat");

    //heading
        JLabel heading = new JLabel("CROP PLAN");
        heading.setBounds(550, 30, 900, 95);
        heading.setFont(new Font("Railway", Font.BOLD, 76));
        add(heading);

    //summary table
        JTable summaryTable = buildSummaryTable(info);
        JScrollPane tableScroll = new JScrollPane(summaryTable);
        tableScroll.setBounds(550, 145, 610, 200);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0), 2));
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(tableScroll);

    //summary chart
        FertilizerChart chart = new FertilizerChart(
                info.n * acres, info.p * acres, info.k * acres, fmt(acres) + " acres (kg)");
        chart.setBounds(1180, 145, 350, 200);
        add(chart);

    //section 1 - crop planning
        JLabel s1 = sectionTitle("Optimize Crop Planning", 365);
        JTextArea b1 = sectionBody(
                "Seed variety: " + info.variety + "\n" +
                "Planting window: " + info.planting + "\n" +
                "Harvest window: " + info.harvest + "\n" +
                "Rotation / intercropping: " + info.rotation,
                405, 185);
        add(s1);
        add(b1);

    //section 2 - resources
        JLabel s2 = sectionTitle("Manage Resources Efficiently", 610);
        JTextArea b2 = sectionBody(
                "Irrigation: " + info.irrigation + "\n" +
                "Fertilizer per acre (rates): N " + fmt(info.n) + " kg, P " + fmt(info.p) +
                        " kg, K " + fmt(info.k) + " kg - totals are shown in the summary table above.\n" +
                "Pest control: " + info.pests + "\n" +
                "Weather / soil: check soil moisture and the forecast before every irrigation or spray, and log field observations weekly.",
                650, 185);
        add(s2);
        add(b2);

    //section 3 - financials
        JLabel s3 = sectionTitle("Track Financial Performance", 855);
        JTextArea b3 = sectionBody(financeText(info), 895, 155);
        add(s3);
        add(b3);

    //button Back
        JButton backButton = new JButton("BACK");
        backButton.setBounds(550, 1060, 450, 50);
        backButton.setFont(new Font("Railway", Font.PLAIN, 30));
        backButton.setForeground(new Color(255, 255, 255));
        backButton.setBackground(new Color(0, 40, 180));
        backButton.addActionListener(e -> {
            if (back != null) back.setVisible(true);
            dispose();
        });
        add(backButton);

    //button Logout
        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setBounds(1050, 1060, 450, 50);
        logoutButton.setFont(new Font("Railway", Font.PLAIN, 30));
        logoutButton.setForeground(new Color(255, 255, 255));
        logoutButton.setBackground(new Color(128, 0, 0));
        logoutButton.addActionListener(e -> {
            if (back != null) back.dispose();
            dispose();
            new Login();
        });
        add(logoutButton);

        setVisible(true);
        storeInDb();
    }

    //summary table with crop, acreage and computed key figures
    private JTable buildSummaryTable(CropInfo info) {
        String[][] rows = {
                {"Crop", crop},
                {"Acreage", fmt(acres) + " acres"},
                {"Seed needed", fmt(info.seedRate * acres) + " kg"},
                {"Fertilizer total", "N " + fmt(info.n * acres) + " / P " + fmt(info.p * acres)
                        + " / K " + fmt(info.k * acres) + " kg"},
                {"Est. yield", fmt(info.yieldPerAcre * acres) + " t"},
                {"Prepared for", userName}
        };
        String[] columns = {"Measure", "Value"};

        JTable table = new JTable(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setFont(new Font("Railway", Font.PLAIN, 20));
        table.setRowHeight(26);
        table.setFocusable(false);
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 200));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(232, 245, 233));
        table.setSelectionForeground(Color.BLACK);
        table.getColumnModel().getColumn(0).setPreferredWidth(210);
        table.getColumnModel().getColumn(1).setPreferredWidth(390);
        table.setPreferredScrollableViewportSize(new Dimension(600, 190));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(232, 245, 233));
                c.setForeground(Color.BLACK);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Railway", Font.BOLD, 20));
        header.setBackground(new Color(0, 100, 0));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(600, 33));
        return table;
    }

    private JLabel sectionTitle(String text, int y) {
        JLabel title = new JLabel(text);
        title.setBounds(550, y, 980, 35);
        title.setFont(new Font("Railway", Font.BOLD, 26));
        title.setForeground(new Color(0, 100, 0));
        return title;
    }

    private JTextArea sectionBody(String text, int y, int height) {
        JTextArea area = new JTextArea(text);
        area.setBounds(550, y, 980, height);
        area.setFont(new Font("Railway", Font.PLAIN, 18));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFocusable(false);
        area.setCursor(Cursor.getDefaultCursor());
        return area;
    }

    private String financeText(CropInfo info) {
        int count = -1;
        try (Connection conn = con.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM expenses WHERE crop_name = ?")) {
            ps.setString(1, crop);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) count = rs.getInt(1);
            }
        } catch (SQLException e) {
            count = -1;
        }

        if (count > 0) {
            return "Cost records: " + count + " expense type(s) registered for " + crop + ".\n"
                    + "Gross return = estimated yield (see summary table) x your local price per tonne.\n"
                    + "ROI = (gross return - total cost) / total cost x 100.\n"
                    + "Review cash flow and compare profitability against other crops before each season.";
        } else if (count == 0) {
            return "No expense records for " + crop
                    + " yet - add seed, fertilizer, labor and equipment costs to the expenses table.\n"
                    + "Once costs are recorded, totals and ROI appear here and in the summary table.\n"
                    + "Centralizing costs lets you manage cash flow and analyze return on investment per crop.";
        }
        return "Cost data is unavailable right now - expense records could not be read.\n"
                + "Keep records of expenses, labor and equipment to calculate profitability, cash flow and ROI.";
    }

    //method to save this plan in the database
    private void storeInDb() {
        String ddl = "CREATE TABLE IF NOT EXISTS crop_plans ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(100) NOT NULL, "
                + "crop VARCHAR(50) NOT NULL, "
                + "acres DOUBLE NOT NULL, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String insert = "INSERT INTO crop_plans(username, crop, acres) VALUES(?,?,?)";

        try (Connection conn = con.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(ddl);
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, userName);
                ps.setString(2, crop);
                ps.setDouble(3, acres);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Plan displayed, but saving failed:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static String fmt(double v) {
        if (v == Math.rint(v)) return String.valueOf((long) v);
        return String.format("%.2f", v);
    }

    public static void main(String[] args) {
        new CropPlanPage(args.length > 0 ? args[0] : "Guest", "Wheat", 5, null);
    }
}

//bar chart graphic for the summary: N-P-K totals in kilograms
class FertilizerChart extends JPanel {
    double n, p, k;
    String caption;

    FertilizerChart(double n, double p, double k, String caption) {
        this.n = n;
        this.p = p;
        this.k = k;
        this.caption = caption;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0), 2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setFont(new Font("Railway", Font.BOLD, 18));
        g2.setColor(new Color(40, 40, 40));
        g2.drawString("Fertilizer for " + caption, 16, 28);

        int baseY = h - 34;
        int topY = 52;
        double max = Math.max(n, Math.max(p, k));
        if (max <= 0) max = 1;

        g2.setColor(new Color(180, 180, 180));
        g2.drawLine(30, baseY, w - 15, baseY);

        int barW = 64;
        int[] xs = {62, 148, 234};
        double[] vals = {n, p, k};
        Color[] colors = {new Color(33, 115, 240), new Color(0, 140, 0), new Color(220, 160, 0)};
        String[] names = {"N", "P", "K"};

        g2.setFont(new Font("Railway", Font.BOLD, 16));
        for (int i = 0; i < 3; i++) {
            int barH = (int) Math.round((baseY - topY) * (vals[i] / max));
            if (vals[i] > 0 && barH < 3) barH = 3;
            int y = baseY - barH;
            if (barH > 0) {
                g2.setColor(colors[i]);
                g2.fillRect(xs[i], y, barW, barH);
            }
            g2.setColor(Color.BLACK);
            g2.drawString(CropPlanPage.fmt(vals[i]), xs[i] + barW / 2 - 14, y - 7);
            g2.drawString(names[i], xs[i] + barW / 2 - 7, baseY + 22);
        }
    }
}
