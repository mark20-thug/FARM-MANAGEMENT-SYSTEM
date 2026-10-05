public class Main {

    public static void main(String[] args) {
        new Login();
    }

    public static void openHome(String username) {
        new Dashboard(username);
    }
}
