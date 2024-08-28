import java.util.*;
import java.sql.*;
import java.text.*;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static Connection connect() {
        String url = "jdbc:oracle:thin:@//db18.cse.cuhk.edu.hk:1521/oradb.cse.cuhk.edu.hk";
        String username = "h004";
        String password = "irhonchA";
        Connection con = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.out.println("[ERROR]: Oracle DB Driver not found!");
            System.exit(0);
        } catch (SQLException e) {
            System.out.println(e);
        }
        return con;
    }

    private static int if_exists() {
        String qry1 = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'BOOK'";
        String qry2 = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'CUSTOMER'";
        String qry3 = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'CORDER'";
        int book_exist = 0, customer_exist = 0, corder_exist = 0;
        try {
            Statement stmt = connect().createStatement();
            ResultSet rs1 = stmt.executeQuery(qry1);

            while (rs1.next()) {
                book_exist = rs1.getInt(1); // if table BOOK, already exist, book_exist equals to 1
            }
            rs1.close();

            ResultSet rs2 = stmt.executeQuery(qry2);

            while (rs2.next()) {
                customer_exist = rs2.getInt(1); // if table CUSTOMER, already exist, customer_exist equals to 1
            }
            rs2.close();

            ResultSet rs3 = stmt.executeQuery(qry3);

            while (rs3.next()) {
                corder_exist = rs3.getInt(1); // if table CORDER, already exist, corder_exist equals to 1
            }
            rs3.close();

            if (book_exist == 1 && customer_exist == 1 && corder_exist == 1) {
                return 1;
            } else
                return 0; // meaning table does not exist

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }

    }

    public static void count_number() {
        int exists = if_exists();

        System.out.println("\nOverview of the database records");
        if (exists == 0) {
            System.out.println("\nDatabase is empty (Tables does not exist)");
        } else {
            String query1 = "SELECT COUNT (*) FROM (SELECT DISTINCT ISBN FROM BOOK)";
            String query2 = "SELECT COUNT (*) FROM (SELECT DISTINCT OID FROM  CORDER)";
            String query3 = "SELECT COUNT (*) FROM Customer";
            try {
                Statement stmt = connect().createStatement();
                ResultSet rs1 = stmt.executeQuery(query1);

                while (rs1.next()) {
                    int totalbook = rs1.getInt(1);

                    System.out.println("Total Number of Book: " + totalbook);
                }
                rs1.close();

                ResultSet rs2 = stmt.executeQuery(query2);
                while (rs2.next()) {
                    int totalorder = rs2.getInt(1);

                    System.out.println("Total Number of Order Records: " + totalorder);
                }
                rs2.close();

                ResultSet rs3 = stmt.executeQuery(query3);
                while (rs3.next()) {
                    int totalcustomer = rs3.getInt(1);

                    System.out.println("Total Number of Customers: " + totalcustomer);
                }
                rs3.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        int input;

        // CompletableFuture.runAsync(() -> MyFrame.start1(null));

        System.out.println("===== Welcome to Book Ordering Management System =====");
        SimpleDateFormat timeFormat;
        SimpleDateFormat dayFormat;
        SimpleDateFormat dateFormat;

        String time;
        String day;
        String date;
        timeFormat = new SimpleDateFormat("hh:mm:ss a");
        dayFormat = new SimpleDateFormat("EEEE");
        dateFormat = new SimpleDateFormat("MMMMM dd, yyyy");

        time = timeFormat.format(Calendar.getInstance().getTime());

        day = dayFormat.format(Calendar.getInstance().getTime());

        date = dateFormat.format(Calendar.getInstance().getTime());
        System.out.println("+ System Date: " + date + " " + day);
        System.out.println("+ System Time: " + time);
        count_number();
        System.out.println("————————————————————————————————");
        System.out.println("1. Database Initialization");
        System.out.println("2. Customer Operation");
        System.out.println("3. Bookstore Operation");
        System.out.println("4. Quit");
        do {
            System.out.println("Please Enter Your Query:");
            input = scanner.nextInt();
            if (input < 1 || input > 4) {
                System.out.println("[ERROR] Invalid input");
            }
        } while (input < 1 || input > 4);

        switch (input) {
            case 1:
                initialization.start(args);
                break;
            case 2:
                customer.start(args);
                break;
            case 3:
                bookstore.start(args);
                break;
            case 4:
                System.out.println("Program Quitted!!!");
                System.exit(0);
                break;
        }
    }
}
