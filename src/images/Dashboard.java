package images;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    String userName;
    public  Dashboard(String userName){
    // screen display
        this.userName = userName;
        JFrame page = new JFrame("Farm_Management_System");
        page.setSize(1600, 1200);
        page.setLocationRelativeTo(null);
        page.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        page.setLayout(null);
    //image
        ImageIcon icon = new ImageIcon(getClass().getResource("images/images.jpeg"));
        Image icon2 = icon.getImage().getScaledInstance(300, 300, Image.SCALE_DEFAULT);
        ImageIcon icon3 = new ImageIcon(icon2);
        JLabel imageLabel = new JLabel(icon3);
        imageLabel.setBounds(20, 20, 600, 600);
        add(imageLabel);
    //heading text
        JLabel heading = new JLabel(" WELCOME TO FARM MANAGEMENT SYSTEM");
        heading.setBounds(280, 50, 1500, 300);
        heading.setFont(new Font("Railway", Font.BOLD, 24));
        add(heading);

        JLabel line = new JLabel("Dear Farmer, Welcome to our Farm Management System");
        line.setBounds(330, 95, 680, 18);
        line.setFont(new Font("Railway", Font.PLAIN, 14));
        add(line);

        JLabel line1 = new JLabel("Here, you can get all Details from sowing to harvesting");
        line1.setBounds(345, 115, 500, 18);
        line1.setFont(new Font("Railway", Font.PLAIN, 14));
        add(line1);

        JLabel line2 = new JLabel("Just enter the crop name and the acreage to get it started");
        line2.setBounds(335, 135, 500, 18);
        line2.setFont(new Font("Railway", Font.PLAIN, 14));
        add(line2);

        JLabel cropLabel = new JLabel("Select Crop: ");
        cropLabel.setBounds(250, 200, 150, 22);
        cropLabel.setFont(new Font("Railway", Font.PLAIN, 20));
        add(cropLabel);
    // crop dropdown
        String[] crops = {"", "Wheat", "Rice", "Soybean","Sugarcane"};
        JComboBox<String> cropDown = new JComboBox<>(crops);
        cropDown.setBounds(370, 200, 150, 25);
        add(cropDown);

        JLabel acresLabel = new JLabel("Acrage: ");
        acresLabel.setBounds(560, 200, 150, 22);
        acresLabel.setFont(new Font("Railway",Font.PLAIN, 20 ));
        add(acresLabel);

        JTextField acresField = new JTextField();
        acresField.setBounds(650, 200, 100, 25);
        add(acresField);



        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Railway", Font.PLAIN, 40));
        submitButton.setBounds(900, 490, 500, 60);
        submitButton.setBackground(new Color(0, 128, 0));
        submitButton.setForeground(new Color(255, 255, 255));
        add(submitButton);

        JButton logoutButton = new JButton("Submit");
        logoutButton.setFont(new Font("Railway", Font.PLAIN, 40));
        logoutButton.setBounds(900, 490, 500, 60);
        logoutButton.setBackground(new Color(0, 128, 0));
        logoutButton.setForeground(new Color(255, 255, 255));
        add(logoutButton);







        setVisible(true);
    }

    public static void main(String[] args){
         new Dashboard(userName);

    }
}
