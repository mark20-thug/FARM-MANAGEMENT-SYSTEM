import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CropPlanPage extends JFrame {

    String userName;
    String crop;
    double acres;
    Dashboard back;

    //expense rows read from the MySQL expenses table (null = could not be read)
    private List<Expense> expenses;

    static class Expense {
        final String type;
        final double costPerAcre;

        Expense(String type, double costPerAcre) {
            this.type = type;
            this.costPerAcre = costPerAcre;
        }
    }

    //cost_per_acre seed values used the first time a crop has no expense rows
    static final Map<String, String[][]> EXPENSE_SEED = new HashMap<>();

    static {
        EXPENSE_SEED.put("Wheat", new String[][]{
                {"Seed", "50"}, {"Fertilizer", "120"}, {"Labor", "80"}, {"Equipment", "40"}});
        EXPENSE_SEED.put("Rice", new String[][]{
                {"Seed", "60"}, {"Fertilizer", "150"}, {"Labor", "100"}, {"Equipment", "50"}});
        EXPENSE_SEED.put("Soybean", new String[][]{
                {"Seed", "40"}, {"Fertilizer", "90"}, {"Labor", "70"}, {"Equipment", "35"}});
        EXPENSE_SEED.put("Sugarcane", new String[][]{
                {"Seed", "200"}, {"Fertilizer", "250"}, {"Labor", "180"}, {"Equipment", "90"}});
    }

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

        seedExpenses();
        loadExpenses();

        setLayout(null);
        setTitle("Farm_Management_System");
        setSize(1600, 1200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CropInfo info = CROPS.get(crop);
        if (info == null) info = CROPS.get("Wheat");

    //heading
        JLabel heading = new JLabel("CROP PLAN");
        heading.setBounds(550, 15, 900, 95);
        heading.setFont(new Font("Railway", Font.BOLD, 76));
        add(heading);

    //summary table
        JTable summaryTable = buildSummaryTable(info);
        JScrollPane tableScroll = new JScrollPane(summaryTable);
        tableScroll.setBounds(550, 125, 610, 190);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0), 2));
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(tableScroll);

    //summary chart
        FertilizerChart chart = new FertilizerChart(
                info.n * acres, info.p * acres, info.k * acres, fmt(acres) + " acres (kg)");
        chart.setBounds(1180, 125, 350, 190);
        add(chart);

    //section 1 - crop planning
        JLabel s1 = sectionTitle("Optimize Crop Planning", 330);
        JScrollPane planScroll = new JScrollPane(infoTable(
                new String[]{"Field", "Details"},
                new String[][]{
                        {"Seed variety", info.variety},
                        {"Planting window", info.planting},
                        {"Harvest window", info.harvest},
                        {"Rotation / intercropping", info.rotation}},
                new int[]{230, 750}));
        planScroll.setBounds(550, 365, 980, 170);
        planScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(s1);
        add(planScroll);

    //section 2 - resources
        JLabel s2 = sectionTitle("Manage Resources Efficiently", 550);
        JScrollPane resourceScroll = new JScrollPane(infoTable(
                new String[]{"Resource", "Details"},
                new String[][]{
                        {"Irrigation", info.irrigation},
                        {"Fertilizer per acre", "N " + fmt(info.n) + " kg, P " + fmt(info.p)
                                + " kg, K " + fmt(info.k) + " kg - totals are in the summary table above."},
                        {"Pest control", info.pests},
                        {"Weather / soil", "Check soil moisture and the forecast before every irrigation or spray; log field observations weekly."}},
                new int[]{210, 770}));
        resourceScroll.setBounds(550, 585, 980, 170);
        resourceScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(s2);
        add(resourceScroll);

    //section 3 - expenses read from MySQL, shown in Ugandan shillings
        JLabel s3 = sectionTitle("Track Financial Performance (UGX)", 770);
        JScrollPane expenseScroll = new JScrollPane(infoTable(
                new String[]{"Item", "Value", "UGX for this plan"},
                expenseRows(),
                new int[]{150, 540, 290}));
        expenseScroll.setBounds(550, 810, 980, 240);
        expenseScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(s3);
        add(expenseScroll);

    //button Back
        JButton backButton = new JButton("BACK");
        backButton.setBounds(550, 1066, 450, 50);
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
        logoutButton.setBounds(1050, 1066, 450, 50);
        logoutButton.setFont(new Font("Railway", Font.PLAIN, 30));
        logoutButton.setForeground(new Color(255, 255, 255));
        logoutButton.setBackground(new Color(128, 0, 0));
        logoutButton.addActionListener(e -> {
            if (back != null) back.dispose();
            dispose();
            new Login();
        });
        add(logoutButton);

    //keep buttons pinned to the bottom whenever the window is laid out or resized
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int btnY = getContentPane().getHeight() - 60;
                backButton.setBounds(550, btnY, 450, 50);
                logoutButton.setBounds(1050, btnY, 450, 50);
            }
        });

        setVisible(true);
        storeInDb();
    }

    //summary table with crop, acreage, computed key figures and recorded expenses
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
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);
        table.setPreferredScrollableViewportSize(new Dimension(600, 149));
        styleTable(table, 17, 24);

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
        return table;
    }

    //two or three column table for a section; cell text wraps and rows grow to fit
    private JTable infoTable(String[] columns, String[][] rows, int[] widths) {
        JTable table = new JTable(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                FontMetrics fm = getFontMetrics(getFont());
                int needed = 0;
                for (int col = 0; col < getColumnCount(); col++) {
                    Object v = getValueAt(row, col);
                    int h = WrapCellRenderer.neededHeight(
                            v == null ? "" : v.toString(), fm, getCellRect(row, col, true).width);
                    needed = Math.max(needed, h);
                }
                if (getRowHeight(row) < needed) {
                    setRowHeight(row, needed);
                }
                return c;
            }
        };
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        styleTable(table, 16, 24);
        table.setDefaultRenderer(Object.class, new WrapCellRenderer());
        return table;
    }

    //shared styling: green header, zebra rows, read-only
    private void styleTable(JTable table, int fontSize, int rowHeight) {
        table.setFont(new Font("Railway", Font.PLAIN, fontSize));
        table.setRowHeight(rowHeight);
        table.setFocusable(false);
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 200));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(232, 245, 233));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        int headerWidth = 0;
        for (javax.swing.table.TableColumn col :
                java.util.Collections.list(table.getColumnModel().getColumns())) {
            headerWidth += col.getPreferredWidth();
        }
        header.setFont(new Font("Railway", Font.BOLD, fontSize));
        header.setBackground(new Color(0, 100, 0));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(headerWidth, 30));
    }

    //greedy word wrap shared by measuring and painting so they always agree
    static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        if (maxWidth < 1) {
            lines.add(text);
            return lines;
        }
        StringBuilder cur = new StringBuilder();
        for (String word : text.split(" ")) {
            if (word.isEmpty()) continue;
            String test = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(test) <= maxWidth) {
                cur.setLength(0);
                cur.append(test);
                continue;
            }
            if (cur.length() > 0) {
                lines.add(cur.toString());
                cur.setLength(0);
            }
            if (fm.stringWidth(word) <= maxWidth) {
                cur.append(word);
                continue;
            }
            for (char ch : word.toCharArray()) {          //force-break an over-long word
                if (cur.length() > 0 && fm.stringWidth(cur.toString() + ch) > maxWidth) {
                    lines.add(cur.toString());
                    cur.setLength(0);
                }
                cur.append(ch);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        if (lines.isEmpty()) lines.add("");
        return lines;
    }

    private JLabel sectionTitle(String text, int y) {
        JLabel title = new JLabel(text);
        title.setBounds(550, y, 980, 35);
        title.setFont(new Font("Railway", Font.BOLD, 26));
        title.setForeground(new Color(0, 100, 0));
        return title;
    }

    //expense rows for the financial table: every amount in Ugandan shillings
    private String[][] expenseRows() {
        if (expenses == null) {
            return new String[][]{
                    {"Cost data unavailable", "Expense records could not be read from MySQL", ""}};
        }
        if (expenses.isEmpty()) {
            return new String[][]{
                    {"No expense records", "Add seed, fertilizer, labor and equipment costs for "
                            + crop + " to the expenses table", ""}};
        }

        String[][] rows = new String[expenses.size() + 3][3];
        int i = 0;
        for (Expense e : expenses) {
            rows[i][0] = e.type;
            rows[i][1] = ugx(e.costPerAcre) + " per acre";
            rows[i][2] = ugx(e.costPerAcre * acres);
            i++;
        }
        double perAcre = costPerAcre();
        rows[i][0] = "Total cost";
        rows[i][1] = ugx(perAcre) + " per acre";
        rows[i][2] = ugx(perAcre * acres);
        rows[i + 1][0] = "Gross return";
        rows[i + 1][1] = "Estimated yield (summary table) x price/tonne";
        rows[i + 1][2] = "";
        rows[i + 2][0] = "ROI";
        rows[i + 2][1] = "(Gross return - total cost) / total cost x 100";
        rows[i + 2][2] = "";
        return rows;
    }

    //per-acre cost summed across every expense row recorded for this crop
    private double costPerAcre() {
        double sum = 0;
        for (Expense e : expenses) sum += e.costPerAcre;
        return sum;
    }

    //create the expenses table if missing and seed rows for this crop when it has none
    private void seedExpenses() {
        String[][] rows = EXPENSE_SEED.get(crop);
        if (rows == null) return;

        String ddl = "CREATE TABLE IF NOT EXISTS expenses ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "crop_name VARCHAR(100) NOT NULL, "
                + "expense_type VARCHAR(100) NOT NULL, "
                + "cost_per_acre DOUBLE NOT NULL)";

        try (Connection conn = con.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(ddl);

            int existing = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM expenses WHERE crop_name = ?")) {
                ps.setString(1, crop);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) existing = rs.getInt(1);
                }
            }
            if (existing > 0) return;

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO expenses(crop_name, expense_type, cost_per_acre) VALUES(?,?,?)")) {
                for (String[] row : rows) {
                    ps.setString(1, crop);
                    ps.setString(2, row[0]);
                    ps.setDouble(3, Double.parseDouble(row[1]));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Expenses could not be initialised:\n" + e.getMessage(),
                    "Database Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    //read every expense row for this crop from MySQL
    private void loadExpenses() {
        expenses = new ArrayList<>();
        try (Connection conn = con.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT expense_type, cost_per_acre FROM expenses WHERE crop_name = ? ORDER BY id")) {
            ps.setString(1, crop);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(new Expense(rs.getString(1), rs.getDouble(2)));
                }
            }
        } catch (SQLException e) {
            expenses = null;
        }
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

    //money is always shown in Ugandan shillings with thousands separators
    static String ugx(double v) {
        return "UGX " + String.format("%,.0f", v);
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

//cell renderer that word-wraps text so long values are never clipped
class WrapCellRenderer extends JPanel implements TableCellRenderer {
    static final int PAD = 8;
    static final int V_PAD = 2;

    private String text = "";

    WrapCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        text = value == null ? "" : value.toString();
        setFont(table.getFont());
        setForeground(Color.BLACK);
        setBackground(isSelected ? table.getSelectionBackground()
                : row % 2 == 0 ? Color.WHITE : new Color(232, 245, 233));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = g2.getFontMetrics();
        List<String> lines = CropPlanPage.wrapText(text, fm, getWidth() - 2 * PAD);
        int blockH = lines.size() * fm.getHeight();
        int y = Math.max(V_PAD, (getHeight() - blockH) / 2) + fm.getAscent();
        for (String line : lines) {
            g2.drawString(line, PAD, y);
            y += fm.getHeight();
        }
        g2.dispose();
    }

    //height a cell needs to show this text fully wrapped
    static int neededHeight(String text, FontMetrics fm, int cellWidth) {
        List<String> lines = CropPlanPage.wrapText(text, fm, cellWidth - 2 * PAD);
        return lines.size() * fm.getHeight() + 2 * V_PAD;
    }
}
