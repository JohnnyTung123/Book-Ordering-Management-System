import java.sql.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class initialization {
    static Connection con = Main.connect();
    private static Scanner scanner = new Scanner(System.in);

    public static void start(String[] myArgs) {
        int choice;

        while (true) {
            System.out.println("\n-----Operations for Database Initialization-----");
            System.out.println("What kinds of operation would you like to perform?");
            System.out.println("1. Initialize Database: Create All Tables");
            System.out.println("2. Load Data");
            System.out.println("3. Reset Database");
            System.out.println("4. Drop All Tables");
            System.out.println("5. Delete All Records");
            System.out.println("6. Back To Main Menu");
            System.out.println("7. Quit Program");

            do {
                System.out.print("Please Enter Your Query:");
                choice = scanner.nextInt();

                if (choice < 1 || choice > 7) {
                    System.out.println("[ERROR] Invalid input");
                }
            } while (choice < 1 || choice > 7);

            switch (choice) {
                case 1:
                    create_tables(choice);
                    break;

                case 2:
                    load_data();
                    System.out.println("Success: All Data Loaded");
                    break;

                case 3:
                    drop_tables(choice);
                    create_tables(choice);
                    load_data();
                    System.out.println("Success: All Tables Resetted!");
                    break;
                case 4:
                    drop_tables(choice);
                    break;
                case 5:
                    delete_records();
                    break;
                case 6: // back to main menu
                    Main.main(myArgs);
                    break;

                case 7: // quit the program
                    System.out.println("Program Quitted");
                    System.exit(0); // this is to quit the program
                    break;

            }
        }
    }

    private static void delete_records() {
        int exists = if_exists();

        try {

            Statement stmt = con.createStatement();
            if (exists == 1) {
                stmt.executeUpdate("DELETE FROM BOOK"); // delete all existing records from corresponding tables
                stmt.executeUpdate("DELETE FROM CUSTOMER");
                stmt.executeUpdate("DELETE FROM CORDER");
                System.out.println("All records Deleted");
            } else
                System.out.println(
                        "Failed to delete records! Reason: You cannot delete records from non-existing tables.");

        } catch (SQLException e) {
            System.out.println("[Error]: " + e.getMessage() + "\n");
            return;
        }

    }

    private static int if_exists() {
        String qry1 = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'BOOK'";
        String qry2 = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'CUSTOMER'";
        String qry3 = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'CORDER'";
        int book_exist = 0, customer_exist = 0, corder_exist = 0;
        try {
            Statement stmt = con.createStatement();
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

    private static void create_tables(int choice) {
        // please note that uid, order are reserved words in Oracle
        int exists = if_exists();

        String book = "CREATE TABLE Book (ISBN varchar(13), Title varchar(100) NOT NULL, Author varchar(50), Price integer NOT NULL, InventoryQuantity integer NOT NULL, PRIMARY KEY (ISBN,Author))";
        String customer = "CREATE TABLE Customer (CUID varchar(10) primary key, Name varchar(50) NOT NULL, Address varchar(200) NOT NULL)";
        String corder = "CREATE TABLE Corder (OID varchar(8), CUID varchar(10) NOT NULL, OrderDate DATE NOT NULL, ISBN varchar(13), OrderQuantity integer NOT NULL, ShippingStatus varchar(8) NOT NULL, PRIMARY KEY (OID,ISBN))";

        try {
            Statement stmt = con.createStatement();
            // create new table if it is not exist
            if (exists == 0) {
                stmt.executeUpdate(book);
                stmt.executeUpdate(customer);
                stmt.executeUpdate(corder);
                if (choice == 1) // as we don't want to show this message when resetting tables
                    System.out.println("Success! Created Three Tables: Book, Customer and Corder");
            } else if (choice == 1)
                System.out.println("Failed to create tables. Reason: tables already exists!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void drop_tables(int choice) {
        int exists = if_exists();

        String book = "DROP TABLE Book";
        String customer = "DROP TABLE Customer";
        String corder = "DROP TABLE Corder";

        try {
            Statement stmt = con.createStatement();
            if (exists == 1) { // if tables exist
                stmt.executeUpdate(book);
                stmt.executeUpdate(customer);
                stmt.executeUpdate(corder);
                if (choice == 4) // as we don't want to show this message when resetting tables
                    System.out.println("Success! Dropped Three Tables: Book, Customers and Corder!");
            } else if (choice == 4)
                System.out.println("Failed to drop Tables. Reason: does not exist.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void load_data() {

        try {
            Statement stmt = con.createStatement();

            stmt.executeUpdate("DELETE FROM BOOK"); // delete all existing records from corresponding tables
            stmt.executeUpdate("DELETE FROM CUSTOMER");
            stmt.executeUpdate("DELETE FROM CORDER");

            File read_file = new File("sample_data//book.txt"); // load data
            Scanner read_book = new Scanner(read_file);

            while (read_book.hasNextLine()) {
                String[] attributes = new String[10000];
                attributes = read_book.nextLine().split("\\t|\\s{2,}"); // check later

                try {
                    // System.out.println(attributes[0]);
                    // System.out.println(attributes[1]);
                    // System.out.println(attributes[2]);
                    // System.out.println(Integer.parseInt(attributes[3]));
                    // System.out.println(Integer.parseInt(attributes[4]));

                    String sql_string = "INSERT INTO Book (ISBN,Title,Author,Price,InventoryQuantity) VALUES('"
                            + attributes[0] + "','" + attributes[1] + "','"
                            + attributes[2] + "',"
                            + Integer.parseInt(attributes[3]) + "," + Integer.parseInt(attributes[4]) + ")";
                    // System.out.println(sql_string);
                    stmt.executeUpdate(sql_string);
                } catch (SQLIntegrityConstraintViolationException e) {
                    continue;
                }
            }
            read_book.close();

            File read_file2 = new File("sample_data//customer.txt");
            Scanner read_book2 = new Scanner(read_file2);

            while (read_book2.hasNextLine()) {
                String[] attributes = new String[10000];
                attributes = read_book2.nextLine().split("\\t|\\s{2,}"); // check later

                try {
                    // System.out.println(attributes[0]);
                    // System.out.println(attributes[1]);
                    // System.out.println(attributes[2]);

                    String sql_string = "INSERT INTO Customer (CUID,Name,Address) VALUES('"
                            + attributes[0] + "','" + attributes[1] + "','"
                            + attributes[2] + "')";
                    // System.out.println(sql_string);
                    stmt.executeUpdate(sql_string);
                } catch (SQLIntegrityConstraintViolationException e) {
                    continue;
                }
            }
            read_book2.close();

            File read_file3 = new File("sample_data//order.txt");
            Scanner read_book3 = new Scanner(read_file3);

            while (read_book3.hasNextLine()) {
                String[] attributes = new String[10000];
                attributes = read_book3.nextLine().split("\\t|\\s{2,}"); // check later

                try {
                    // System.out.println(attributes[0]);
                    // System.out.println(attributes[1]);
                    // System.out.println(attributes[2]);
                    // System.out.println(attributes[3]);
                    // System.out.println(Integer.parseInt(attributes[4]));
                    // System.out.println(attributes[5]);

                    String sql_string = "INSERT INTO Corder (OID,CUID, OrderDate, ISBN, OrderQuantity, ShippingStatus) VALUES('"
                            + attributes[0] + "','" + attributes[1] + "',TO_DATE('" + attributes[2]
                            + "', 'YYYY-MM-DD'),'"
                            + attributes[3]
                            + "'," + Integer.parseInt(attributes[4]) + ",'" + attributes[5] + "')";

                    // System.out.println(sql_string);
                    stmt.executeUpdate(sql_string);
                } catch (SQLIntegrityConstraintViolationException e) {
                    continue;
                }
            }
            read_book3.close();

        } catch (FileNotFoundException e) {
        } catch (SQLException e) {
            System.out.println("[Error]: " + e.getMessage() + "\n");
            return;
        }

    }
}
