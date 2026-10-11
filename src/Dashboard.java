import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Dashboard extends JFrame {
    String userName;
    JComboBox<String> cropDown;
    JTextField acresField;

    private static final Color GREEN = new Color(0, 100, 0);
    private static final Color BTN_GREEN = new Color(0, 128, 0);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ZEBRA = new Color(232, 245, 233);
    private static final Color GRAY = new Color(90, 90, 90);
    private static final String[] CROPS = {"Wheat", "Rice", "Soybean", "Sugarcane"};
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat CREATED_FMT = new SimpleDateFormat("dd MMM yyyy, HH:mm");

    private static final String[] TABLE_COLS = {
            "", "Crop", "Acres", "Cost / acre (UGX)", "Est. cost (UGX)", "Created"};
    private static final int COL_ID = 0, COL_CROP = 1, COL_ACRES = 2,
            COL_UNIT = 3, COL_TOTAL = 4, COL_CREATED = 5;

    private JLabel refreshedLabel, statusLabel;
    private JLabel kpiPlans, kpiAcres, kpiCrops, kpiBudget;
    private JLabel kpiPlansSub, kpiAcresSub, kpiCropsSub, kpiBudgetSub;
    private JLabel estimateLabel, profileLabel;
    private CropAcresChart chart;
    private JTable plansTable;
    private DefaultTableModel plansModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JButton openButton, removeButton, refreshButton;
    private final JButton[] chips = new JButton[CROPS.length];

    private Map<String, Double> dbCostPerAcre = new HashMap<>();
    private boolean dbErrorShown;

    public Dashboard(String userName) {
        this.userName = userName;
        setLayout(null);
        setTitle("Farm_Management_System");
        setSize(1600, 1200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildSidebar();
        buildHeader();
        buildKpiCards();
        buildChartAndProfile();
        buildPlanner();
        buildPlansTable();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutSidebar();
                layoutMain();
            }
        });

        setVisible(true);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            SwingUtilities.invokeLater(this::loadData);
        }
    }

    private JPanel sidebar;

    private void buildSidebar() {
        sidebar = new JPanel(null);
        sidebar.setBackground(GREEN);
        add(sidebar);
        layoutSidebar();
    }

    private JLabel imageLabel, sideTitle, welcomeLabel, dateLabel, versionLabel;
    private JButton dashButton, planButton, sideRefreshButton, logoutButton;

    private void layoutSidebar() {
        sidebar.setBounds(0, 0, 470, getContentPane().getHeight());

        if (imageLabel == null) {
            ImageIcon icon = new ImageIcon(getClass().getResource("images/images.jpeg"));
            Image scaled = icon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(scaled));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
            sidebar.add(imageLabel);

            sideTitle = new JLabel("<html><div style='text-align:center'>FARM MANAGEMENT<br>SYSTEM</div></html>");
            sideTitle.setFont(new Font("Railway", Font.BOLD, 38));
            sideTitle.setForeground(Color.WHITE);
            sideTitle.setHorizontalAlignment(SwingConstants.CENTER);
            sidebar.add(sideTitle);

            welcomeLabel = new JLabel("<html><div style='text-align:center'>Welcome,<br>" + userName + "</div></html>");
            welcomeLabel.setFont(new Font("Railway", Font.PLAIN, 26));
            welcomeLabel.setForeground(Color.WHITE);
            welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sidebar.add(welcomeLabel);

            dateLabel = new JLabel(new SimpleDateFormat("EEEE, d MMMM yyyy").format(new Date()));
            dateLabel.setFont(new Font("Railway", Font.PLAIN, 18));
            dateLabel.setForeground(new Color(210, 235, 210));
            dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sidebar.add(dateLabel);

            dashButton = navButton("DASHBOARD", true);
            dashButton.addActionListener(e -> loadData());
            sidebar.add(dashButton);

            planButton = navButton("PLAN A CROP", false);
            planButton.addActionListener(e -> {
                cropDown.requestFocusInWindow();
                if (cropDown.getSelectedIndex() <= 0 && cropDown.isShowing()) cropDown.showPopup();
            });
            sidebar.add(planButton);

            sideRefreshButton = navButton("REFRESH DATA", false);
            sideRefreshButton.addActionListener(e -> loadData());
            sidebar.add(sideRefreshButton);

            logoutButton = navButton("LOGOUT", false, new Color(128, 0, 0));
            logoutButton.addActionListener(e -> {
                dispose();
                new Login();
            });
            sidebar.add(logoutButton);

            versionLabel = new JLabel("Farm Management System  -  version 1");
            versionLabel.setFont(new Font("Railway", Font.PLAIN, 15));
            versionLabel.setForeground(new Color(190, 225, 190));
            versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sidebar.add(versionLabel);
        }

        imageLabel.setBounds(85, 25, 280, 280);
        sideTitle.setBounds(25, 330, 420, 110);
        welcomeLabel.setBounds(25, 450, 420, 80);
        dateLabel.setBounds(25, 535, 420, 32);

        int y = 600;
        for (JButton b : new JButton[]{dashButton, planButton, sideRefreshButton, logoutButton}) {
            b.setBounds(40, y, 390, 54);
            y += 66;
        }
        versionLabel.setBounds(25, getContentPane().getHeight() - 45, 420, 30);
    }

    private JButton navButton(String text, boolean active) {
        return navButton(text, active, active ? Color.WHITE : new Color(0, 82, 0));
    }

    private JButton navButton(String text, boolean active, Color base) {
        JButton b = new JButton(text);
        b.setFont(new Font("Railway", Font.PLAIN, 24));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setForeground(active ? GREEN : Color.WHITE);
        b.setBackground(base);
        b.setBorder(BorderFactory.createLineBorder(new Color(220, 240, 220), 1));
        final Color hover = active ? new Color(235, 246, 235) : base.brighter();
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(base);
            }
        });
        return b;
    }

    private JLabel headingLabel;

    private void buildHeader() {
        headingLabel = new JLabel("FARM DASHBOARD");
        headingLabel.setFont(new Font("Railway", Font.BOLD, 58));
        headingLabel.setForeground(new Color(30, 30, 30));
        add(headingLabel);

        JLabel subtitle = new JLabel("Plan, track and cost every acre from sowing to harvest.");
        subtitle.setFont(new Font("Railway", Font.PLAIN, 24));
        subtitle.setForeground(GRAY);
        add(subtitle);
        subHeader = subtitle;

        refreshedLabel = new JLabel(" ");
        refreshedLabel.setFont(new Font("Railway", Font.PLAIN, 18));
        refreshedLabel.setForeground(GRAY);
        refreshedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(refreshedLabel);

        layoutMain();
    }

    private JLabel subHeader;

    private void buildKpiCards() {
        JLabel[] plans = makeCard(510, "MY PLANS", "crop plans recorded",
                "Crop plans saved for your account");
        kpiPlans = plans[0];
        kpiPlansSub = plans[1];

        JLabel[] acres = makeCard(775, "TOTAL ACRES", "acres across all plans",
                "Sum of the acreage across your plans");
        kpiAcres = acres[0];
        kpiAcresSub = acres[1];

        JLabel[] crops = makeCard(1040, "CROPS GROWN", "distinct crops in rotation",
                "Distinct crops across your plans");
        kpiCrops = crops[0];
        kpiCropsSub = crops[1];

        JLabel[] budget = makeCard(1305, "PLANNED BUDGET", "seed + fertilizer + labour",
                "Estimated seed, fertilizer, labour and equipment cost");
        kpiBudget = budget[0];
        kpiBudgetSub = budget[1];
    }

    private JLabel[] makeCard(int x, String caption, String sub, String tip) {
        JPanel card = new JPanel(null);
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(GREEN, 2));
        card.setToolTipText(tip);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(BTN_GREEN, 3));
                card.setBackground(new Color(246, 252, 246));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(GREEN, 2));
                card.setBackground(CARD_BG);
            }
        });

        JLabel cap = new JLabel(caption);
        cap.setFont(new Font("Railway", Font.BOLD, 15));
        cap.setForeground(GREEN);
        cap.setBounds(14, 10, 217, 24);
        card.add(cap);

        JLabel value = new JLabel("--");
        value.setFont(new Font("Railway", Font.BOLD, 38));
        value.setForeground(new Color(30, 30, 30));
        value.setBounds(14, 34, 217, 50);
        card.add(value);

        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(new Font("Railway", Font.PLAIN, 14));
        subLabel.setForeground(GRAY);
        subLabel.setBounds(14, 92, 217, 28);
        card.add(subLabel);

        add(card);
        card.setBounds(x, 190, 245, 130);
        cards.add(card);
        return new JLabel[]{value, subLabel};
    }

    private final List<JPanel> cards = new ArrayList<>();

    private void buildPlanner() {
        JLabel title = new JLabel("PLAN A NEW CROP");
        title.setFont(new Font("Railway", Font.BOLD, 26));
        title.setForeground(GREEN);
        plannerTitle = title;
        add(title);

        JPanel form = new JPanel(null);
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createLineBorder(GREEN, 2));
        add(form);
        formPanel = form;

        JLabel cropLabel = new JLabel("SELECT CROP:");
        cropLabel.setFont(new Font("Railway", Font.PLAIN, 22));
        cropLabel.setBounds(15, 14, 165, 40);
        form.add(cropLabel);

        cropDown = new JComboBox<>(new String[]{"", "Wheat", "Rice", "Soybean", "Sugarcane"});
        cropDown.setFont(new Font("Railway", Font.PLAIN, 22));
        cropDown.setBounds(185, 14, 300, 40);
        cropDown.addActionListener(e -> {
            refreshChips();
            updateEstimates();
        });
        form.add(cropDown);

        for (int i = 0; i < CROPS.length; i++) {
            final String crop = CROPS[i];
            JButton chip = new JButton(crop);
            chip.setFont(new Font("Railway", Font.PLAIN, 15));
            chip.setFocusPainted(false);
            chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chip.setBounds(15 + i * 120, 64, 110, 32);
            chip.addActionListener(e -> cropDown.setSelectedItem(crop));
            form.add(chip);
            chips[i] = chip;
        }
        refreshChips();

        JLabel acresLabel = new JLabel("ACREAGE:");
        acresLabel.setFont(new Font("Railway", Font.PLAIN, 22));
        acresLabel.setBounds(15, 106, 165, 40);
        form.add(acresLabel);

        acresField = new JTextField();
        acresField.setFont(new Font("Railway", Font.PLAIN, 22));
        acresField.setBounds(185, 106, 300, 40);
        acresField.addActionListener(e -> submitDetails());
        acresField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateEstimates(); }
            @Override public void removeUpdate(DocumentEvent e) { updateEstimates(); }
            @Override public void changedUpdate(DocumentEvent e) { updateEstimates(); }
        });
        form.add(acresField);

        JPanel estimate = new JPanel(null);
        estimate.setBackground(new Color(245, 250, 245));
        estimate.setBorder(BorderFactory.createLineBorder(new Color(180, 205, 180), 1));
        estimate.setBounds(15, 154, 470, 102);
        form.add(estimate);

        estimateLabel = new JLabel();
        estimateLabel.setFont(new Font("Railway", Font.PLAIN, 15));
        estimateLabel.setForeground(new Color(40, 40, 40));
        estimateLabel.setBounds(10, 6, 450, 90);
        estimate.add(estimateLabel);

        JButton submit = new JButton("SUBMIT PLAN");
        submit.setFont(new Font("Railway", Font.PLAIN, 24));
        submit.setForeground(Color.WHITE);
        submit.setBackground(BTN_GREEN);
        submit.setFocusPainted(false);
        submit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submit.setBounds(15, 264, 470, 40);
        submit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                submit.setBackground(new Color(0, 155, 0));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                submit.setBackground(BTN_GREEN);
            }
        });
        submit.addActionListener(e -> submitDetails());
        form.add(submit);

        layoutMain();
        updateEstimates();
    }

    private JLabel plannerTitle;
    private JPanel formPanel;
    private JPanel chartPanel, profilePanel;
    private JLabel plansTitle;
    private JScrollPane tableScroll;
    private JPanel statusBar;

    private void buildChartAndProfile() {
        chart = new CropAcresChart();
        chart.setBackground(Color.WHITE);
        chart.setBorder(BorderFactory.createLineBorder(GREEN, 2));
        add(chart);
        chartPanel = chart;

        profilePanel = new JPanel(null);
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(BorderFactory.createLineBorder(GREEN, 2));
        add(profilePanel);

        profileLabel = new JLabel("<html><b>Crop profile</b><br>Select a crop to see its variety,"
                + " planting and harvest windows.</html>");
        profileLabel.setFont(new Font("Railway", Font.PLAIN, 15));
        profileLabel.setForeground(new Color(40, 40, 40));
        profileLabel.setBounds(12, 10, 496, 115);
        profilePanel.add(profileLabel);

        layoutMain();
    }

    private void buildPlansTable() {
        plansTitle = new JLabel("YOUR RECENT PLANS");
        plansTitle.setFont(new Font("Railway", Font.BOLD, 26));
        plansTitle.setForeground(GREEN);
        add(plansTitle);

        searchField = new JTextField();
        searchField.setFont(new Font("Railway", Font.PLAIN, 18));
        searchField.setToolTipText("Type a crop name to filter your plans");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        add(searchField);

        openButton = smallButton("OPEN", BTN_GREEN);
        openButton.setEnabled(false);
        openButton.setToolTipText("Load the selected plan into the planner (or double-click a row)");
        openButton.addActionListener(e -> openSelectedPlan());
        add(openButton);

        refreshButton = smallButton("REFRESH", new Color(0, 70, 160));
        refreshButton.addActionListener(e -> loadData());
        add(refreshButton);

        removeButton = smallButton("REMOVE", new Color(128, 0, 0));
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> removeSelectedPlan());
        add(removeButton);

        plansModel = new DefaultTableModel(TABLE_COLS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case COL_ID:
                        return Integer.class;
                    case COL_ACRES:
                    case COL_UNIT:
                    case COL_TOTAL:
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };
        plansTable = new JTable(plansModel);
        plansTable.setRowHeight(28);
        plansTable.setFont(new Font("Railway", Font.PLAIN, 16));
        plansTable.setFocusable(false);
        plansTable.setShowGrid(true);
        plansTable.setGridColor(new Color(200, 200, 200));
        plansTable.setIntercellSpacing(new Dimension(1, 1));
        plansTable.setSelectionBackground(ZEBRA);
        plansTable.setSelectionForeground(Color.BLACK);
        plansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plansTable.getTableHeader().setReorderingAllowed(false);

        PlanCellRenderer renderer = new PlanCellRenderer();
        for (int i = 0; i < plansTable.getColumnCount(); i++) {
            plansTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        int[] widths = {0, 150, 90, 210, 210, 250};
        for (int i = 0; i < widths.length; i++) {
            plansTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        plansTable.removeColumn(plansTable.getColumnModel().getColumn(COL_ID));

        JTableHeader header = plansTable.getTableHeader();
        header.setFont(new Font("Railway", Font.BOLD, 16));
        header.setBackground(GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(1000, 32));

        sorter = new TableRowSorter<>(plansModel);
        plansTable.setRowSorter(sorter);

        plansTable.getSelectionModel().addListSelectionListener(e -> {
            boolean has = plansTable.getSelectedRow() >= 0;
            openButton.setEnabled(has);
            removeButton.setEnabled(has);
        });
        plansTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && plansTable.getSelectedRow() >= 0) {
                    openSelectedPlan();
                }
            }
        });

        tableScroll = new JScrollPane(plansTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(GREEN, 2));
        add(tableScroll);

        statusBar = new JPanel(null);
        statusBar.setBackground(new Color(245, 248, 245));
        statusBar.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 210), 1));
        add(statusBar);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Railway", Font.PLAIN, 17));
        statusLabel.setForeground(GRAY);
        statusLabel.setBounds(12, 4, 1015, 28);
        statusBar.add(statusLabel);

        layoutMain();
    }

    private JButton smallButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Railway", Font.PLAIN, 16));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final Color base = bg;
        final Color hover = bg.brighter();
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(base);
            }
        });
        return b;
    }

    private void layoutMain() {
        int h = getContentPane().getHeight();

        if (headingLabel != null) {
            headingLabel.setBounds(510, 35, 700, 80);
            subHeader.setBounds(510, 120, 750, 40);
            refreshedLabel.setBounds(1150, 55, 400, 40);
        }

        int[] cardX = {510, 775, 1040, 1305};
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setBounds(cardX[i], 190, 245, 130);
        }

        if (plannerTitle != null) {
            plannerTitle.setBounds(510, 345, 600, 35);
            formPanel.setBounds(510, 390, 500, 310);
            chartPanel.setBounds(1030, 390, 520, 160);
            profilePanel.setBounds(1030, 565, 520, 135);
        }

        if (plansTitle != null) {
            plansTitle.setBounds(510, 745, 500, 35);
            searchField.setBounds(760, 748, 300, 34);
            openButton.setBounds(1070, 748, 130, 34);
            refreshButton.setBounds(1210, 748, 160, 34);
            removeButton.setBounds(1380, 748, 170, 34);
            tableScroll.setBounds(510, 790, 1040, Math.max(160, h - 790 - 90));
            statusBar.setBounds(510, h - 85, 1040, 36);
        }
    }

    private void refreshChips() {
        String selected = (String) cropDown.getSelectedItem();
        for (int i = 0; i < chips.length; i++) {
            boolean on = CROPS[i].equals(selected);
            chips[i].setBackground(on ? BTN_GREEN : Color.WHITE);
            chips[i].setForeground(on ? Color.WHITE : GREEN);
            chips[i].setBorder(BorderFactory.createLineBorder(on ? BTN_GREEN : GREEN, on ? 2 : 1));
        }
    }

    private void updateEstimates() {
        if (estimateLabel == null || profileLabel == null) return;
        String crop = selectedCrop();
        CropPlanPage.CropInfo info = crop == null ? null : CropPlanPage.CROPS.get(crop);

        if (info == null) {
            estimateLabel.setText("<html><b>Live estimate</b><br>Pick a crop (or tap a chip above) and enter"
                    + " the acreage to see seed,<br>yield and cost figures update as you type.</html>");
            profileLabel.setText("<html><b>Crop profile</b><br>Select a crop to see its variety,"
                    + " planting and harvest windows.</html>");
            return;
        }

        double unitCost = costFor(crop);
        profileLabel.setText("<html><b>" + crop + "</b> &nbsp;-&nbsp; " + info.variety
                + "<br>Planting: " + info.planting
                + "<br>Harvest: " + info.harvest
                + "<br>Yield " + CropPlanPage.fmt(info.yieldPerAcre) + " t/acre &nbsp;&bull;&nbsp; inputs "
                + CropPlanPage.ugx(unitCost) + "/acre</html>");

        String text = acresField.getText().trim();
        double acres;
        try {
            acres = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            acres = -1;
        }

        if (acres <= 0) {
            estimateLabel.setText("<html><b>Live estimate - " + crop + "</b><br>"
                    + "Seed: " + CropPlanPage.fmt(info.seedRate) + " kg/acre<br>"
                    + "Yield: " + CropPlanPage.fmt(info.yieldPerAcre) + " t/acre<br>"
                    + "Inputs: " + CropPlanPage.ugx(unitCost) + "/acre &nbsp; - enter the acreage for totals</html>");
            return;
        }

        estimateLabel.setText("<html><b>Live estimate</b><br>"
                + "Seed needed: " + CropPlanPage.fmt(info.seedRate * acres) + " kg ("
                + CropPlanPage.fmt(info.seedRate) + " kg/acre)<br>"
                + "Est. yield: " + CropPlanPage.fmt(info.yieldPerAcre * acres) + " t<br>"
                + "Est. input cost: " + CropPlanPage.ugx(unitCost * acres) + " ("
                + CropPlanPage.ugx(unitCost) + "/acre)</html>");
    }

    private String selectedCrop() {
        String crop = (String) cropDown.getSelectedItem();
        return crop == null || crop.isEmpty() ? null : crop;
    }

    private double costFor(String crop) {
        Double dbValue = dbCostPerAcre.get(crop);
        if (dbValue != null) return dbValue;

        String[][] seed = CropPlanPage.EXPENSE_SEED.get(crop);
        if (seed == null) return 0;
        double sum = 0;
        for (String[] row : seed) sum += Double.parseDouble(row[1]);
        return sum;
    }

    private void submitDetails() {
        String crop = selectedCrop();
        String acres = acresField.getText().trim();

        if (crop == null || acres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a crop and enter the acreage",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double acresValue;
        try {
            acresValue = Double.parseDouble(acres);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Acreage must be a number",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (acresValue <= 0) {
            JOptionPane.showMessageDialog(this, "Acreage must be greater than zero",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        setVisible(false);
        new CropPlanPage(userName, crop, acresValue, this);
    }

    static class Plan {
        int id;
        String crop;
        double acres;
        double costPerAcre;
        String created;
    }

    private void loadData() {
        List<Plan> plans = new ArrayList<>();
        Map<String, Double> costs = readCostPerAcre();
        String error = null;

        try (Connection conn = con.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, crop, acres, created_at FROM crop_plans WHERE username = ? "
                             + "ORDER BY created_at DESC, id DESC")) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Plan plan = new Plan();
                    plan.id = rs.getInt(1);
                    plan.crop = rs.getString(2);
                    plan.acres = rs.getDouble(3);
                    Timestamp created = rs.getTimestamp(4);
                    plan.created = created == null ? "-" : CREATED_FMT.format(created);
                    plan.costPerAcre = costFor(plan.crop, costs);
                    plans.add(plan);
                }
            }
        } catch (SQLException e) {
            error = e.getMessage();
        }

        dbCostPerAcre = costs;
        fillTable(plans);
        updateKpis(plans);
        updateChart(plans);
        updateEstimates();

        String stamp = TIME_FMT.format(new Date());
        refreshedLabel.setText("Updated " + stamp);

        if (error != null) {
            statusLabel.setForeground(new Color(160, 0, 0));
            statusLabel.setText("Live data unavailable - " + error);
            if (!dbErrorShown) {
                dbErrorShown = true;
                JOptionPane.showMessageDialog(this,
                        "Dashboard data could not be loaded:\n" + error,
                        "Database Error", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            dbErrorShown = false;
            String shown = sorter == null ? "0" : String.valueOf(plansTable.getRowCount());
            statusLabel.setForeground(GRAY);
            statusLabel.setText(plans.isEmpty()
                    ? "No plans yet - pick a crop and acreage above to create your first plan."
                    : "Showing " + shown + " of " + plans.size() + " plans  •  updated " + stamp);
        }
    }

    private Map<String, Double> readCostPerAcre() {
        Map<String, Double> costs = new HashMap<>();
        try (Connection conn = con.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT crop_name, SUM(cost_per_acre) FROM expenses GROUP BY crop_name")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    costs.put(rs.getString(1), rs.getDouble(2));
                }
            }
        } catch (SQLException e) {
            return costs;
        }
        return costs;
    }

    private double costFor(String crop, Map<String, Double> costs) {
        Double value = costs.get(crop);
        if (value != null) return value;

        String[][] seed = CropPlanPage.EXPENSE_SEED.get(crop);
        if (seed == null) return 0;
        double sum = 0;
        for (String[] row : seed) sum += Double.parseDouble(row[1]);
        return sum;
    }

    private void fillTable(List<Plan> plans) {
        plansModel.setRowCount(0);
        for (Plan plan : plans) {
            plansModel.addRow(new Object[]{
                    plan.id, plan.crop, plan.acres, plan.costPerAcre,
                    plan.costPerAcre * plan.acres, plan.created});
        }
        if (sorter != null) {
            applyFilter();
            sorter.sort();
        }
    }

    private void updateKpis(List<Plan> plans) {
        double totalAcres = 0;
        double budget = 0;
        Map<String, Boolean> crops = new LinkedHashMap<>();
        for (Plan plan : plans) {
            totalAcres += plan.acres;
            budget += plan.acres * plan.costPerAcre;
            crops.put(plan.crop, Boolean.TRUE);
        }
        kpiPlans.setText(String.valueOf(plans.size()));
        kpiAcres.setText(CropPlanPage.fmt(totalAcres));
        kpiCrops.setText(String.valueOf(crops.size()));
        kpiBudget.setText(formatBudget(budget));

        kpiPlansSub.setText(plans.size() == 1 ? "crop plan recorded" : "crop plans recorded");
        kpiAcresSub.setText("acres across all plans");
        kpiCropsSub.setText(crops.size() == 1 ? "distinct crop in rotation" : "distinct crops in rotation");
        kpiBudgetSub.setText("seed + fertilizer + labour + equipment");
    }

    static String formatBudget(double v) {
        if (v >= 1_000_000_000) return String.format("%.1fB", v / 1_000_000_000);
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
        if (v >= 1_000) return String.format("%.1fK", v / 1_000);
        return CropPlanPage.fmt(v);
    }

    private void updateChart(List<Plan> plans) {
        Map<String, Double> byCrop = new LinkedHashMap<>();
        for (String crop : CROPS) byCrop.put(crop, 0.0);
        for (Plan plan : plans) {
            byCrop.merge(plan.crop, plan.acres, Double::sum);
        }
        chart.setData(byCrop);
    }

    private void applyFilter() {
        if (sorter == null) return;
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    for (int i = 1; i < entry.getValueCount(); i++) {
                        Object value = entry.getValue(i);
                        if (value != null && value.toString().toLowerCase().contains(query)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        if (statusLabel != null && plansModel.getRowCount() > 0) {
            statusLabel.setForeground(GRAY);
            statusLabel.setText("Showing " + plansTable.getRowCount() + " of "
                    + plansModel.getRowCount() + " plans");
        }
    }

    private int selectedModelRow() {
        int viewRow = plansTable.getSelectedRow();
        if (viewRow < 0) return -1;
        return plansTable.convertRowIndexToModel(viewRow);
    }

    private void openSelectedPlan() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a plan first",
                    "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String crop = (String) plansModel.getValueAt(row, COL_CROP);
        double acres = ((Number) plansModel.getValueAt(row, COL_ACRES)).doubleValue();
        cropDown.setSelectedItem(crop);
        acresField.setText(CropPlanPage.fmt(acres));
        statusLabel.setForeground(new Color(0, 70, 160));
        statusLabel.setText("Plan for " + crop + " loaded into the planner - press SUBMIT PLAN to save a new copy.");
        acresField.requestFocusInWindow();
        acresField.selectAll();
    }

    private void removeSelectedPlan() {
        int row = selectedModelRow();
        if (row < 0) return;
        String crop = (String) plansModel.getValueAt(row, COL_CROP);
        int id = (Integer) plansModel.getValueAt(row, COL_ID);

        int choice = JOptionPane.showConfirmDialog(this,
                "Remove the " + crop + " plan from your records?",
                "Confirm removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        try (Connection conn = con.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM crop_plans WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();
            statusLabel.setForeground(new Color(0, 70, 160));
            statusLabel.setText("Plan removed.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not remove the plan:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class PlanCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String text;
            if (value instanceof Double) {
                double v = (Double) value;
                text = column == 1 ? CropPlanPage.fmt(v) : CropPlanPage.ugx(v);
            } else {
                text = value == null ? "" : value.toString();
            }
            setText(text);
            setHorizontalAlignment(column == 0 || column == 4 ? SwingConstants.LEFT : SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setForeground(Color.BLACK);
            setBackground(isSelected ? ZEBRA : (row % 2 == 0 ? Color.WHITE : ZEBRA));
            return this;
        }
    }

    public static void main(String[] args) {
        new Dashboard(args.length > 0 ? args[0] : "Guest");
    }
}

class CropAcresChart extends JPanel {
    private static final String[] CROPS = {"Wheat", "Rice", "Soybean", "Sugarcane"};
    private static final Color[] COLORS = {
            new Color(0, 128, 0), new Color(33, 115, 240),
            new Color(220, 160, 0), new Color(0, 77, 64)};

    private final Map<String, Double> acresByCrop = new LinkedHashMap<>();

    CropAcresChart() {
        for (String crop : CROPS) acresByCrop.put(crop, 0.0);
    }

    void setData(Map<String, Double> data) {
        for (String crop : CROPS) {
            Double value = data.get(crop);
            acresByCrop.put(crop, value == null ? 0.0 : value);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setFont(new Font("Railway", Font.BOLD, 17));
        g2.setColor(new Color(40, 40, 40));
        g2.drawString("Planned acres by crop", 14, 26);

        double max = 0;
        boolean any = false;
        for (double v : acresByCrop.values()) {
            if (v > 0) any = true;
            max = Math.max(max, v);
        }
        if (max <= 0) max = 1;

        if (!any) {
            g2.setFont(new Font("Railway", Font.PLAIN, 15));
            g2.setColor(new Color(120, 120, 120));
            g2.drawString("No plans yet - your chart fills in after the first plan.", 14, 60);
            return;
        }

        int left = 95;
        int right = w - 78;
        int top = 40;
        int barH = 20;
        int gap = 8;

        g2.setFont(new Font("Railway", Font.PLAIN, 15));
        for (int i = 0; i < CROPS.length; i++) {
            String crop = CROPS[i];
            double value = acresByCrop.get(crop);
            int y = top + i * (barH + gap);

            g2.setColor(new Color(60, 60, 60));
            g2.drawString(crop, 12, y + barH - 5);

            int barW = (int) Math.round((right - left) * (value / max));
            if (value > 0 && barW < 4) barW = 4;
            g2.setColor(new Color(235, 235, 235));
            g2.fillRect(left, y, right - left, barH);
            if (value > 0) {
                g2.setColor(COLORS[i]);
                g2.fillRect(left, y, barW, barH);
            }

            g2.setColor(new Color(60, 60, 60));
            String label = CropPlanPage.fmt(value) + (value == 1 ? " acre" : " acres");
            g2.drawString(label, right + 6, y + barH - 5);
        }
    }
}
