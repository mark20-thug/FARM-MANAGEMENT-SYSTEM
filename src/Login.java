
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends JFrame {

    JTextField nameField;
    JPasswordField passwordField;

    public Login(){
//page sizes

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
        imageLabel.setBounds(20, 20, 600, 600);
        add(imageLabel);

//heading text
        JLabel heading = new JLabel("FARM MANAGEMENT SYSTEM");
        heading.setBounds(145, 20, 1500, 100);
        heading.setFont(new Font("Railway", Font.BOLD, 76));
        add(heading);
//login text
        JLabel Login = new JLabel("(LOGIN PAGE)");
        Login.setBounds(650, 140, 600, 70);
        Login.setFont(new Font("Railway", Font.PLAIN, 40));
        add(Login);

//name and nameField
        JLabel name = new JLabel("NAME: ");
        name.setBounds(660, 280, 300, 50);
        name.setFont(new Font("Railway", Font.PLAIN, 40));
        add(name);

        nameField = new JTextField();
        nameField.setBounds(900, 280, 500,  50);
        nameField.setFont(new Font("Railway", Font.PLAIN, 40));
        add(nameField);

//password and passwordField
        JLabel password = new JLabel("PASSWORD: ");
        password.setBounds(660, 380, 300, 50);
        password.setFont(new Font("Railway", Font.PLAIN, 40));
        add(password);

        passwordField = new JPasswordField();
        passwordField.setBounds(900, 380, 500,  50);
        passwordField.setFont(new Font("Railway", Font.PLAIN, 50));
        passwordField.setEchoChar('*');
        add(passwordField);

//password checkbox
        JCheckBox showPassword =  new JCheckBox("Show Password");
        showPassword.setBounds(902, 440, 500, 30);
        showPassword.setFont(new Font("Railway", Font.PLAIN, 25));
        showPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(showPassword.isSelected()){
                    passwordField.setEchoChar((char)0);
            }else{

                    passwordField.setEchoChar('*');
                }
            }
        });
        add(showPassword);

//Login button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Railway", Font.PLAIN, 40));
        loginButton.setBounds(900, 490, 500, 60);
        loginButton.setBackground(new Color(0, 128, 0));
        loginButton.setForeground(new Color(255, 255, 255));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });
        add(loginButton);

        JLabel text1 = new JLabel("Don't have an account, Please");
        text1.setBounds(660, 590, 500, 40);
        text1.setFont(new Font("Railway", Font.PLAIN, 32));
        add(text1);

        JLabel text2 = new JLabel("Register Here");
        text2.setBounds(1160, 590, 300, 44);
        text2.setFont(new Font("Railway", Font.PLAIN, 36));
        text2.setForeground(new Color(0, 62, 250, 255));
        text2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        text2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new Register();
            }
        });
        add(text2);

        setVisible(true);
    }
    private void attemptLogin() {
        String username = nameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both name and password.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = con.getConnection();
             PreparedStatement pstm = conn.prepareStatement(query)) {

            pstm.setString(1, username);

            boolean matched = false;
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String stored = rs.getString("password");
                    if (PasswordHasher.isEncoded(stored)) {
                        if (PasswordHasher.verify(password, stored)) {
                            matched = true;
                            break;
                        }
                    } else if (password.equals(stored)) {
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                JOptionPane.showMessageDialog(this, "Invalid name or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                return;
            }

            JOptionPane.showMessageDialog(this, "Login Successful, welcome " + username + "!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            Main.openHome(username);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Login failed:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args){
        new Login();
    }
}