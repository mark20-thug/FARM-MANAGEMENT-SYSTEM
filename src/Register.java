
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class Register extends JFrame {
    JTextField nameField, numberField, emailField, districtField, passwordField, confirmPasswordField;
    JDateChooser dateChooser;
    JRadioButton male, Female;

    public Register() {
        setLayout(null);
        setTitle("Farm_Management_System");
        setSize(1600, 1200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//image
        ImageIcon icon = new ImageIcon(getClass().getResource("images/hills.jpeg"));
        Image icon2 = icon.getImage().getScaledInstance(300, 300, Image.SCALE_DEFAULT);
        ImageIcon icon3 = new ImageIcon(icon2);

        JLabel imageLabel = new JLabel(icon3);
        imageLabel.setBounds(20, 20, 500, 600);
        add(imageLabel);
//heading
        JLabel heading = new JLabel("Registration Page");
        heading.setBounds(550, 30, 800, 95);
        heading.setFont(new Font("Railway", Font.BOLD, 76));
        add(heading);
//name and name field
        JLabel name = new JLabel("NAME: ");
        name.setBounds(550, 180, 300, 50);
        name.setFont(new Font("Railway", Font.PLAIN, 30));
        add(name);

        nameField = new JTextField();
        nameField.setBounds(950, 180, 500,  50);
        nameField.setFont(new Font("Railway", Font.PLAIN, 30));
        add(nameField);
//date and dateChooser
        JLabel date = new JLabel("DATE OF BIRTH: ");
        date.setBounds(550, 280, 300, 50);
        date.setFont(new Font("Railway", Font.PLAIN, 30));
        add(date);

        dateChooser = new JDateChooser();
        dateChooser.setBounds(950, 280, 500, 50);
        dateChooser.setFont(new Font("Railway", Font.PLAIN, 25));
        add(dateChooser);
//Gender field
        JLabel gender = new JLabel("GENDER: ");
        gender.setBounds(550, 380, 300, 50);
        gender.setFont(new Font("Railway", Font.PLAIN, 30));
        add(gender);
    // Gender Radio Buttons
        male = new JRadioButton("Male");
        male.setBounds(950, 380, 100, 50);
        male.setFont(new Font("Railway", Font.PLAIN, 30));
        add(male);

        Female = new JRadioButton("Female");
        Female.setBounds(1320, 380, 200, 50);
        Female.setFont(new Font("Railway", Font.PLAIN, 30));
        add(Female);

        ButtonGroup group = new ButtonGroup();
        group.add(male);
        group.add(Female);
//Phone Number
        JLabel number = new JLabel("PHONE NUMBER: ");
        number.setBounds(550, 480, 600, 50);
        number.setFont(new Font("Railway", Font.PLAIN, 30));
        add(number);

        numberField = new JTextField();
        numberField.setBounds(950, 480, 500,  50);
        numberField.setFont(new Font("Railway", Font.PLAIN, 30));
        add(numberField);
//email
        JLabel email = new JLabel("EMAIL: ");
        email.setBounds(550, 580, 600, 50);
        email.setFont(new Font("Railway", Font.PLAIN, 30));
        add(email);

        emailField = new JTextField();
        emailField.setBounds(950, 580, 500,  50);
        emailField.setFont(new Font("Railway", Font.PLAIN, 30));
        add(emailField);
//District
        JLabel district = new JLabel("DISTRICT: ");
        district.setBounds(550, 680, 600, 50);
        district.setFont(new Font("Railway", Font.PLAIN, 30));
        add(district);

        districtField = new JTextField();
        districtField.setBounds(950, 680, 500,  50);
        districtField.setFont(new Font("Railway", Font.PLAIN, 30));
        add(districtField);

//password
        JLabel password = new JLabel("PASSWORD: ");
        password.setBounds(550, 780, 600, 50);
        password.setFont(new Font("Railway", Font.PLAIN, 30));
        add(password);

        passwordField = new JTextField();
        passwordField.setBounds(950, 780, 500,  50);
        passwordField.setFont(new Font("Railway", Font.PLAIN, 30));
        add(passwordField);
    //confirm password
        JLabel confirmPassword = new JLabel("CONFIRM PASSWORD: ");
        confirmPassword.setBounds(550, 880, 680, 50);
        confirmPassword.setFont(new Font("Railway", Font.PLAIN, 30));
        add(confirmPassword);

        confirmPasswordField = new JTextField();
        confirmPasswordField.setBounds(950, 880, 500,  50);
        confirmPasswordField.setFont(new Font("Railway", Font.PLAIN, 30));
        add(confirmPasswordField);

//button Register

    JButton register = new JButton("REGISTER");
    register.setBounds(800, 980, 500, 50);
    register.setFont(new Font("Railway", Font.PLAIN, 30));
    register.setForeground(new Color(255, 255, 255));
    register.setBackground(new Color(0, 128, 0));
    register.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(validateField()){
                storeInDb();
            }
        }
    });
    add(register);
//lower text
    JLabel text1 = new JLabel("if you already have and account");
    text1.setBounds(550, 1080,500, 30);
    text1.setFont(new Font("RAILWAY", Font.PLAIN, 30));
    add(text1);

    JLabel text2 = new JLabel("LOGIN");
    text2.setBounds(1100, 1080,200, 30);
    text2.setFont(new Font("RAILWAY", Font.PLAIN, 30));
    text2.setForeground(new Color(0, 40, 255));
    text2.setCursor(new Cursor(Cursor.HAND_CURSOR));
    text2.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            dispose();
            new Login();
        }
    });
    add(text2);

        setVisible(true);
    }

    //method to validate input field

    private boolean validateField(){
        if(nameField.getText().trim().isEmpty()|| numberField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() || districtField.getText().trim().isEmpty() ||
            passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()
            || dateChooser.getDate()==null ||(!male.isSelected() && !Female.isSelected())){

            JOptionPane.showMessageDialog(this, "Please Fill All Fields", "Error", JOptionPane.ERROR_MESSAGE);

            return  false;
        }
        if(!passwordField.getText().equals(confirmPasswordField.getText())){
            JOptionPane.showMessageDialog(this, "Password do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;

    }
// methode store data in the database
    private void storeInDb(){
        String username = nameField.getText().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dob = sdf.format(dateChooser.getDate());
        String gender = male.isSelected()? "male" : "female";
        String mobile = numberField.getText().trim();
        String email = emailField.getText().trim();
        String district = districtField.getText().trim();
        String encodedPassword = PasswordHasher.encode(passwordField.getText());

        String duplicateQuery = "SELECT id FROM users WHERE email = ? OR mobile = ?";

        try (Connection conn = con.getConnection()) {

            try (PreparedStatement check = conn.prepareStatement(duplicateQuery)) {
                check.setString(1, email);
                check.setString(2, mobile);
                try (java.sql.ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "An account with this email or phone number already exists.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            String query = "INSERT INTO users(username, dob, gender, mobile, email, district, password)VALUES(?,?,?,?,?,?,?)";

            try (PreparedStatement pstm = conn.prepareStatement(query)) {
                pstm.setString(1, username);
                pstm.setString(2, dob);
                pstm.setString(3, gender);
                pstm.setString(4, mobile);
                pstm.setString(5, email);
                pstm.setString(6, district);
                pstm.setString(7, encodedPassword);

                pstm.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Registration Successful","Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new Login();

        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this,
                    "An account with this email or phone number already exists.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Registration failed:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public static void main(String[] args){

        new Register();
    }

}
