import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        new Login();
    }

    // TODO: replace this placeholder with your own Page.
    public static void openHome(String username) {
        JFrame page = new JFrame("Farm_Management_System");
        page.setSize(1600, 1200);
        page.setLocationRelativeTo(null);
        page.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        page.setLayout(null);

        JLabel heading = new JLabel("FARM MANAGEMENT SYSTEM");
        heading.setBounds(165, 60, 1300, 100);
        heading.setFont(new Font("Railway", Font.BOLD, 76));
        page.add(heading);

        JLabel welcome = new JLabel("Welcome, " + username);
        welcome.setBounds(165, 220, 1300, 80);
        welcome.setFont(new Font("Railway", Font.PLAIN, 48));
        page.add(welcome);

        JButton logout = new JButton("LOGOUT");
        logout.setBounds(165, 360, 400, 70);
        logout.setFont(new Font("Railway", Font.PLAIN, 40));
        logout.setBackground(new Color(128, 0, 0));
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> {
            page.dispose();
            new Login();
        });
        page.add(logout);

        page.setVisible(true);
    }
}
