import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    String userName;
    JComboBox<String> cropDown;
    JTextField acresField;

    public Dashboard(String userName) {
    // screen display
        this.userName = userName;
        setLayout(null);
        setTitle("Farm_Management_System");
        setSize(1600, 1200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //image
        ImageIcon icon = new ImageIcon(getClass().getResource("images/images.jpeg"));
        Image icon2 = icon.getImage().getScaledInstance(300, 300, Image.SCALE_DEFAULT);
        ImageIcon icon3 = new ImageIcon(icon2);
        JLabel imageLabel = new JLabel(icon3);
        imageLabel.setBounds(20, 20, 500, 600);
        add(imageLabel);
    //heading text
        JLabel heading = new JLabel("FARM MANAGEMENT SYSTEM");
        heading.setBounds(550, 30, 1030, 95);
        heading.setFont(new Font("Railway", Font.BOLD, 76));
        add(heading);

        JLabel welcome = new JLabel("Welcome, " + userName);
        welcome.setBounds(550, 150, 900, 50);
        welcome.setFont(new Font("Railway", Font.PLAIN, 36));
        add(welcome);

        JLabel line = new JLabel("Dear Farmer, Welcome to our Farm Management System");
        line.setBounds(550, 230, 900, 40);
        line.setFont(new Font("Railway", Font.PLAIN, 24));
        add(line);

        JLabel line1 = new JLabel("Here, you can get all Details from sowing to harvesting");
        line1.setBounds(550, 280, 900, 40);
        line1.setFont(new Font("Railway", Font.PLAIN, 24));
        add(line1);

        JLabel line2 = new JLabel("Just enter the crop name and the acreage to get it started");
        line2.setBounds(550, 330, 900, 40);
        line2.setFont(new Font("Railway", Font.PLAIN, 24));
        add(line2);

    // crop label and dropdown
        JLabel cropLabel = new JLabel("SELECT CROP:");
        cropLabel.setBounds(550, 450, 350, 50);
        cropLabel.setFont(new Font("Railway", Font.PLAIN, 30));
        add(cropLabel);

        String[] crops = {"", "Wheat", "Rice", "Soybean", "Sugarcane"};
        cropDown = new JComboBox<>(crops);
        cropDown.setBounds(950, 450, 500, 50);
        cropDown.setFont(new Font("Railway", Font.PLAIN, 30));
        add(cropDown);

    // acreage label and field
        JLabel acresLabel = new JLabel("ACREAGE:");
        acresLabel.setBounds(550, 550, 350, 50);
        acresLabel.setFont(new Font("Railway", Font.PLAIN, 30));
        add(acresLabel);

        acresField = new JTextField();
        acresField.setBounds(950, 550, 500, 50);
        acresField.setFont(new Font("Railway", Font.PLAIN, 30));
        add(acresField);

    //button Submit
        JButton submitButton = new JButton("SUBMIT");
        submitButton.setBounds(800, 670, 500, 50);
        submitButton.setFont(new Font("Railway", Font.PLAIN, 30));
        submitButton.setForeground(new Color(255, 255, 255));
        submitButton.setBackground(new Color(0, 128, 0));
        submitButton.addActionListener(e -> submitDetails());
        add(submitButton);

    //button Logout
        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setBounds(800, 760, 500, 50);
        logoutButton.setFont(new Font("Railway", Font.PLAIN, 30));
        logoutButton.setForeground(new Color(255, 255, 255));
        logoutButton.setBackground(new Color(128, 0, 0));
        logoutButton.addActionListener(e -> {
            dispose();
            new Login();
        });
        add(logoutButton);

        setVisible(true);
    }

    //method to validate crop and acreage before showing the result
    private void submitDetails() {
        String crop = (String) cropDown.getSelectedItem();
        String acres = acresField.getText().trim();

        if (crop == null || crop.isEmpty() || acres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a crop and enter the acreage",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Double.parseDouble(acres);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Acreage must be a number",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double acresValue = Double.parseDouble(acres);
        setVisible(false);
        new CropPlanPage(userName, crop, acresValue, this);
    }

    public static void main(String[] args){
         new Dashboard(args.length > 0 ? args[0] : "Guest");
    }
}
