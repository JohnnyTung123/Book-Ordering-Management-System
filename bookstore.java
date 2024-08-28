import java.util.Scanner;
import java.sql.*;

public class bookstore {
    static Connection conn = Main.connect();

    public static void start(String[] myArgs) {
        do {
            System.out.println("\n-----Operations for bookstore menu-----");
            System.out.println("What kinds of operation would you like to perform?");
            System.out.println("1. Order Update");
            System.out.println("2. Order Query");
            System.out.println("3. N Most Popular Books");
            System.out.println("4. Back To Main Menu");
            System.out.println("5. Quit Program");
            System.out.print("Enter Your Choice: ");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            while (choice < 1 || choice > 5) {
                System.out.println("[ERROR] Invalid input. Please enter [1-5].");
                System.out.print("Enter Your Choice: ");
                choice = scanner.nextInt();
            }

            switch (choice) {
                case 1:
                    orderupdate(myArgs);
                    break;

                case 2:
                    orderquery(myArgs);
                    break;

                case 3:
                    nmostpopularbooks(myArgs);
                    break;

                case 4:
                    Main.main(myArgs);
                    break;
                case 5: // quit the program
                    System.out.println("Program Quitted");
                    System.exit(0); // this is to quit the program
                    break;
            }
            // scanner.close();
        } while (true);

    }

    public static String check_currentstatus(int updateOID) {
        /* query for search shipping status */
        String currentshippingstatus = null;
        String query = "SELECT DISTINCT ShippingStatus" +
                " FROM CORDER" +
                " WHERE OID = " + updateOID;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                currentshippingstatus = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return currentshippingstatus;
    }

    public static int check_npopluar(String query) {
        int nnumber = 0;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                nnumber = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return nnumber;
    }

    public static void orderquery(String[] myArgs) {
        System.out.println("Choose the Search criterion:");
        System.out.println("1. All orders with shipping status: ordered");
        System.out.println("2. All orders with shipping status: shipped");
        System.out.println("3. All orders with shipping status: received");
        System.out.println("4. Return to Previous Menu");
        System.out.println("5. Quit Program");
        System.out.print("Choose the search criterion: ");
        Scanner scanner = new Scanner(System.in);
        int searchCriterion = scanner.nextInt();
        scanner.nextLine();
        String searchCriterion2 = "";
        while (searchCriterion < 1 || searchCriterion > 5) {
            System.out.println("[ERROR] Invalid input. Please enter [1-5].");
            System.out.print("Enter Your Choice: ");
            searchCriterion = scanner.nextInt();
        }
        // scanner.close();
        switch (searchCriterion) {
            case 1:
                searchCriterion2 = "ordered";
                break;

            case 2:
                searchCriterion2 = "shipped";
                break;

            case 3:
                searchCriterion2 = "received";
                break;

            case 4:
                bookstore.start(myArgs);
                break;
            case 5: // quit the program
                System.out.println("Program Quitted");
                System.exit(0);
                break;
        }
        String query = "SELECT DISTINCT OID, CUID, SHIPPINGSTATUS" +
                       " FROM CORDER" +
                       " WHERE SHIPPINGSTATUS = '" + searchCriterion2 +
                       "' ORDER BY CAST (OID AS INTEGER) ASC";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.isBeforeFirst()) {
                System.out.println("| OID | UID | Shipping Status |");
                while (rs.next()) {
                    int oid = rs.getInt(1);
                    int uid = rs.getInt(2);
                    String shipping_status = rs.getString(3);
                    System.out.println("| " + oid + " | " + uid + " | " + shipping_status +
                            " | ");
                }
                rs.close();
                System.out.println("End of Query");
            } else {
                System.out.println("Empty: There is no orders with shipping status = " + searchCriterion2);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void orderupdate(String[] myArgs) {
        System.out.println("Please input the OID where you would like to update: ");
        Scanner scanner = new Scanner(System.in);
        int updateOID = scanner.nextInt();
        scanner.nextLine();

        while (check_currentstatus(updateOID) == null) {
            System.out.println("[ERROR] Invalid OID. Please input a valid OID.");
            System.out.print("Please input the OID where you would like to update: ");
            updateOID = scanner.nextInt();
        }

        System.out.println("The current shipping status is: " + check_currentstatus(updateOID));

        System.out.println("Please input the new shipping status");
        System.out.println("1. ordered");
        System.out.println("2. shipped");
        System.out.println("3. received");
        System.out.println("4. Return to Previous Menu");
        System.out.println("5. Quit Program");
        System.out.print("Choose the new shipping status: ");
        int newshippingstatusint = scanner.nextInt();
        scanner.nextLine();

        while (newshippingstatusint < 1 || newshippingstatusint > 5) {
            System.out.println("[ERROR] Invalid input. Please enter [1-5].");
            System.out.print("Enter Your Choice: ");
            newshippingstatusint = scanner.nextInt();
        }
        // scanner.close();
        String newshippingstatus = "";
        switch (newshippingstatusint) {
            case 1:
                newshippingstatus = "ordered";
                break;

            case 2:
                newshippingstatus = "shipped";
                break;

            case 3:
                newshippingstatus = "received";
                break;

            case 4:
                bookstore.start(myArgs);
                break;
            case 5: // quit the program
                System.out.println("Program Quitted");
                System.exit(0);
                break;
        }

        // System.out.println(newshippingstatus);
        // System.out.println(check_currentstatus(updateOID));
        if (check_currentstatus(updateOID).equals("received")) {
            System.out.println("FAILED: order has already been received.");
        } else if (check_currentstatus(updateOID).equals("shipped") && newshippingstatus.equals("ordered")) {
            System.out.println("FAILED: order has already been shipped.");
        } else if (check_currentstatus(updateOID).equals("shipped") && newshippingstatus.equals("shipped")) {
            System.out.println("FAILED: order has already been shipped.");
        } else if (check_currentstatus(updateOID).equals("ordered") && newshippingstatus.equals("ordered")) {
            System.out.println("FAILED: order has already been ordered.");
        } else if (check_currentstatus(updateOID).equals("ordered") && newshippingstatus.equals("received")) {
            System.out.println("FAILED: order has not been shipped.");
        } else {
            String query3 = "UPDATE CORDER SET shippingstatus = '" + newshippingstatus + "' WHERE oid = " + updateOID;
            try {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(query3);
                System.out.print("SUCCESS: order shipping status has already been updated.");
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void nmostpopularbooks(String[] myArgs) {
        System.out.println("Choose input the N most popular books:");
        Scanner scanner = new Scanner(System.in);
        int N = scanner.nextInt();
        scanner.nextLine();
        String query4 = "SELECT DISTINCT b.ISBN, b.Title, a.authors, b.Price, b.InventoryQuantity, R.total_ordered_quantity"
                +
                " FROM Book b, (SELECT O.ISBN, SUM(O.ORDERQUANTITY) AS total_ordered_quantity FROM CORDER O GROUP BY O.ISBN) R, (SELECT ISBN, LISTAGG(author, ', ') WITHIN GROUP (ORDER BY author) AS authors FROM book GROUP BY ISBN) A"
                +
                " WHERE b.ISBN = r.ISBN AND b.ISBN = a.ISBN" +
                " ORDER BY R.total_ordered_quantity DESC ";
        String query5 = "SELECT COUNT (DISTINCT ISBN ) AS Numbers FROM (" + query4 + ")";

        while (N < 1 || N > check_npopluar(query5)) {
            System.out.println("[ERROR] Invalid input. We do not have " + N
                    + " books that have been ordered, please input an integer smaller than or equal to "
                    + check_npopluar(query5));
            System.out.print("Enter Your Choice: ");
            N = scanner.nextInt();
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.setMaxRows(N);
            ResultSet rs = stmt.executeQuery(query4);
            System.out.println("The " + N + " most popular books are: ");
            System.out.println("| ISBN | Title | Author | Price | Inventory Quantity | Total Ordered Quantity |");
            while (rs.next()) {
                String isbn = rs.getString(1);
                String title = rs.getString(2);
                String author = rs.getString(3);
                int price = rs.getInt(4);
                int InventoryQuantity = rs.getInt(5);
                int OrderedQuantity = rs.getInt(6);
                System.out.println("| " + isbn + " | " + title + " | " + author + " | " + price +
                        " | " + InventoryQuantity + " | " + OrderedQuantity + " | ");
            }
            rs.close();
            System.out.println("End of Query");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}