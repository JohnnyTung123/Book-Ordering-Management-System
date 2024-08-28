import java.util.Scanner;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class customer {
    static Connection con = Main.connect();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        start(args);
    }

    public static void start(String[] myArgs) {
        Scanner user = new Scanner(System.in);
        String uid = null;

        while (true) {
            // Prompt the user to enter their user ID
            System.out.print("Enter your user ID: ");
            uid = user.nextLine();

            // Check if the user ID exists in the database
            try {
                Statement stmt = con.createStatement();
                String query = "SELECT * FROM Customer WHERE CUID = '" + uid + "'";
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                    rs.close();
                    stmt.close();
                    break; // Exit the loop if the UID exists
                }
                rs.close();
                stmt.close();
                System.out.println("[ERROR] User ID does not exist. Please try again.");
            } catch (SQLException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }

        int choice;
        while (true) {
            System.out.println("\n-----Operations for Customer-----");
            System.out.println("What kinds of operation would you like to perform?");
            System.out.println("1. Book Search");
            System.out.println("2. Place an Order");
            System.out.println("3. Check History Orders");
            System.out.println("4. Back To Main Menu");
            System.out.println("5. Quit Program");

            do {
                System.out.print("Please Enter Your Query: ");
                choice = scanner.nextInt();

                if (choice < 1 || choice > 5) {
                    System.out.println("[ERROR] Invalid input");
                }
            } while (choice < 1 || choice > 5);

            switch (choice) {
                case 1:
                    bookSearch();
                    break;

                case 2:
                    placeOrder(uid);
                    break;

                case 3:
                    checkHistoryOrder(uid);
                    break;
                case 4: // back to main menu
                    Main.main(myArgs);
                    break;
                case 5: // quit the program
                    System.out.println("Program Quitted");
                    System.exit(0); // this is to quit the while loop in order to quit the program
                    break;

            }
        }
    }

    public static void bookSearch() {
        System.out.println("Choose the Search criterion:");
        System.out.println("1. ISBN");
        System.out.println("2. Book Title");
        System.out.println("3. Author Name");
        System.out.print("Choose the search criterion: ");

        Scanner scanner = new Scanner(System.in);
        int searchCriterion = scanner.nextInt();
        scanner.nextLine();

        // check if the input is valid
        if (searchCriterion < 1 || searchCriterion > 3) {
            System.out.println("[ERROR] Invalid input");
            return;
        }

        String searchCriterion2 = "";
        if (searchCriterion == 1) {
            searchCriterion2 = "ISBN";
        }
        if (searchCriterion == 2) {
            searchCriterion2 = "Title";
        }
        if (searchCriterion == 3) {
            searchCriterion2 = "Author";
        }

        System.out.print("Enter the " + searchCriterion2 + " to search for: ");
        String searchTerm = scanner.nextLine();

        try {
            Statement stmt = con.createStatement();
            String query = "SELECT DISTINCT ISBN, Title, authors, Price, InventoryQuantity FROM (SELECT DISTINCT b.ISBN, b.Title, a.authors, b.Author, b.Price, b.InventoryQuantity  FROM Book b, (SELECT ISBN, LISTAGG(author, ', ') WITHIN GROUP (ORDER BY author) AS authors FROM book GROUP BY ISBN) A WHERE b.ISBN = a.ISBN) WHERE "
                    + searchCriterion2 + " = '" + searchTerm + "'";

            ResultSet rs = stmt.executeQuery(query);
            boolean found = false;
            while (rs.next()) {
                found = true;
                String ISBN = rs.getString("ISBN");
                String title = rs.getString("Title");
                String authors = rs.getString("Authors");
                double price = rs.getDouble("Price");
                int inventoryQuantity = rs.getInt("InventoryQuantity");
                System.out.println("ISBN: " + ISBN + ", Title: " + title + ", Authors: " + authors + ", Price: " + price
                        + ", Inventory Quantity: " + inventoryQuantity);
            }
            if (!found) {
                System.out.println("No book found");
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    public static void placeOrder(String uid) {
        // Prompt the user to enter each book they want to order
        Scanner scanner = new Scanner(System.in);
        List<String> ISBNs = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        // Get current date as a format of 05-APR-23
        java.util.Date date = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MMM-yy");
        String currentDate = sdf.format(date);

        // Prompt the user to enter each book they want to order
        while (true) {
            System.out.println("Enter the ISBN of the book you want to order (or 'done' to finish):");
            String input = scanner.nextLine();
            if (input.equals("done")) {
                break;
            }
            if (ISBNs.contains(input)) {
                System.out.println("[ERROR] ISBN already entered. Please enter a different ISBN.");
                continue; // skip to the next iteration
            }
            ISBNs.add(input);

            System.out.println("Enter the quantity of the book you want to order:");
            int quantity = scanner.nextInt();
            // require the user to enter a positive quantity
            while (quantity <= 0) {
                System.out.println("[ERROR] Invalid quantity. Please enter a positive quantity.");
                System.out.println("Enter the quantity of the book you want to order:");
                quantity = scanner.nextInt();
            }
            quantities.add(quantity);
            scanner.nextLine();
        }

        // Check inventory levels for each book and update the database as necessary
        boolean success = true;
        String errorMessage = "";
        try {
            // generate a unique OID for the new order
            Statement stmt = con.createStatement();
            String query = "SELECT COUNT(*) FROM (SELECT DISTINCT OID FROM COrder)";
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            int numOrders = rs.getInt(1);
            // Convert the number of orders to a string
            String newOID = Integer.toString(numOrders + 1);

            for (int i = 0; i < ISBNs.size(); i++) {
                String ISBN = ISBNs.get(i);
                int quantity = quantities.get(i);
                query = "SELECT * FROM Book WHERE ISBN = '" + ISBN + "'";
                rs = stmt.executeQuery(query);
                if (rs.next()) {
                    int inventoryQuantity = rs.getInt("InventoryQuantity");
                    if (quantity > inventoryQuantity) {
                        success = false;
                        errorMessage += "Inventory shortage for ISBN " + ISBN + "\n";
                    }
                } else {
                    success = false;
                    errorMessage += "No such book for ISBN " + ISBN + "\n";
                }
                rs.close();
            }
            stmt.close();

            // If all books are in stock and exist in the database, insert the order and
            // update the inventory levels
            if (success) {
                for (int i = 0; i < ISBNs.size(); i++) {
                    String ISBN = ISBNs.get(i);
                    int quantity = quantities.get(i);
                    query = "INSERT INTO COrder VALUES ('" + newOID + "', '" + uid + "', '" + currentDate + "', '"
                            + ISBN
                            + "', " + quantity + ", 'ordered')";
                    stmt = con.createStatement();
                    stmt.executeUpdate(query);
                    stmt.close();

                    query = "UPDATE Book SET InventoryQuantity = InventoryQuantity - " + quantity + " WHERE ISBN = '"
                            + ISBN + "'";
                    stmt = con.createStatement();
                    stmt.executeUpdate(query);
                    stmt.close();
                }
                System.out.println("Order placed successfully.");
            } else {
                System.out.println("[ERROR] Order failed:\n" + errorMessage);
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    public static void checkHistoryOrder(String uid) {
        try {
            Statement stmt = con.createStatement();
            String query = "SELECT * FROM CORDER WHERE CUID = '" + uid + "' Order by CAST (OID AS INTEGER) ASC";
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.isBeforeFirst()) {
                System.out.println("No history orders.");
            } else {
                while (rs.next()) {
                    String OID = rs.getString("OID");
                    String UID = rs.getString("CUID");
                    Date OrderDate = rs.getDate("OrderDate");
                    String ISBN = rs.getString("ISBN");
                    String OrderQuantity = rs.getString("OrderQuantity");
                    String ShippingStatus = rs.getString("ShippingStatus");
                    System.out.println("OID: " + OID + ", UID: " + UID + ", OrderDate: " + OrderDate
                            + ", ISBN: " + ISBN
                            + ", OrderQuantity: " + OrderQuantity + ", ShippingStatus: " + ShippingStatus);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

}
