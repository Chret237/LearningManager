package com.example.librarymanager.Controllers;

import com.example.librarymanager.Database.BooksTable;
import com.example.librarymanager.Database.CategoryTable;
import com.example.librarymanager.Models.Books;
import com.example.librarymanager.Models.Category;
import com.example.librarymanager.utils.Alertmessage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BookController implements Initializable {
    @FXML
    private Button addBookButton;
    @FXML
    private TextField authorField;
    @FXML
    private TextField availableCopiesField;
    @FXML
    private TextField categoryField;
    @FXML
    private Button chooseImageButton;
    @FXML
    private TextField isbnField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField totalCopiesField;
    @FXML
    private TextField yearField;
    @FXML
    private GridPane bookGrid;
    @FXML
    private TextField imageNameField;

    private List<Books> books;

    private List<Category> categories;

    private CategoryTable categoryTable = new CategoryTable();

    private BooksTable booksTable = new BooksTable();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categories = getCategories();
        try {
            // Initialize the grid with proper error handling
            if (bookGrid == null) {
                System.err.println("Error: bookGrid is null. FXML might not be loaded correctly.");
                return;
            }

            // Configure grid properties
            bookGrid.setHgap(20); // Set horizontal gap between columns
            bookGrid.setVgap(20); // Set vertical gap between rows
            bookGrid.setPadding(new Insets(20)); // Set padding around the grid

            // Initialize books list and populate grid
            books = new ArrayList<>(data());
            refreshBookGrid();

        } catch (Exception e) {
            System.err.println("Error initializing BookController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Books> data() {
        List<Books> books = new ArrayList<>();
        try {
            books = booksTable.listAll();
            for (Books book : books) {
                // Verify if the image exists in the classpath
                if (getClass().getClassLoader().getResource(book.getImage_path()) == null) {
                    System.err.println("Warning: Image not found in classpath: " + book.getImage_path());
                    book.setImage_path(""); // Set empty path if image not found
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching data from the database: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error creating sample book data: " + e.getMessage());
            e.printStackTrace();
        }
        return books;
    }

    @FXML
    void handleAddBook(ActionEvent event) {
        String title = titleField.getText();
        String author = authorField.getText();
        String category = categoryField.getText();
        String isbn = isbnField.getText();
        String year = yearField.getText();
        String totalCopies = totalCopiesField.getText();
        String availableCopies = availableCopiesField.getText();
        String imageName = imageNameField.getText();
        int totalCopies_parseInt ;
        int availableCopies_parseInt ;
        try {
           totalCopies_parseInt = Integer.parseInt(totalCopies);
          availableCopies_parseInt = Integer.parseInt(availableCopies);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid total copies format");
        }

        if (title.isEmpty() || author.isEmpty() || category.isEmpty() || isbn.isEmpty() || year.isEmpty()
                || totalCopies.isEmpty() || availableCopies.isEmpty() || imageName.isEmpty()) {
            Alertmessage.showAlert("Error", "All field are required");
            return ;
        }

       if( availableCopies_parseInt > totalCopies_parseInt){
        Alertmessage.showAlert("Error", "available Copies should be less than total Copies");
        return ;
       }

        Books newBook = new Books();
        newBook.setTitle(title);
        newBook.setAuthor(author);
        int category_id = getCategoryIdByName(category);
        newBook.setCategory(category_id);
        newBook.setIsbn(isbn);
        newBook.setYear(year);
        newBook.setTotal_copies(totalCopies_parseInt);
        newBook.setAvailable_copies(availableCopies_parseInt);
        if (imageName != null && !imageName.trim().isEmpty()) {
            newBook.setImage_path("Images/" + imageName.trim() + ".jpg");
        } else {
            newBook.setImage_path(""); // or set a default image path if desired
        }
        // Add the new book to the database
        try {
            booksTable.create(newBook);
            Alertmessage.showAlert("Success", "Book added successfully");
            if (books == null) {
                books = new ArrayList<>();
            }
            books.add(newBook);
            refreshBookGrid();
        } catch (SQLException e) {
            System.err.println("Error adding book to database: " + e.getMessage());
            Alertmessage.showAlert("Error", "Failed to add book to database");
        } catch (Exception e) {
            System.err.println("Error adding book to database: " + e.getMessage());
            Alertmessage.showAlert("Error", "Failed to add book to database");
        }
    }

    @FXML
    void handleChooseImage(ActionEvent event) {
        // Implémentez cette méthode
    }

    private void refreshBookGrid() {
        bookGrid.getChildren().clear();
        int columns = 0;
        int rows = 0;

        for (Books book : books) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/Fxml/BookCard.fxml"));

                if (fxmlLoader.getLocation() == null) {
                    System.err.println("Error: Could not find BookCard.fxml resource");
                    continue;
                }

                VBox bookBox = fxmlLoader.load();
                BookCardController bookController = fxmlLoader.getController();

                if (bookController == null) {
                    System.err.println("Error: BookCardController is null");
                    continue;
                }

                bookController.setData(book);

                // Configure book card layout
                bookBox.setPrefWidth(200);
                bookBox.setPrefHeight(300);
                bookBox.setAlignment(Pos.CENTER);
                bookBox.setStyle("-fx-padding: 10;");

                // Add to grid with proper positioning
                GridPane.setMargin(bookBox, new Insets(10));
                bookGrid.add(bookBox, columns, rows);

                // Update grid position
                columns++;
                if (columns >= 4) {
                    columns = 0;
                    rows++;
                }

            } catch (IOException e) {
                System.err.println("Error loading book card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        try {
            // Fetch categories from the database
            CategoryTable CategoryTable = new CategoryTable();
            categories = CategoryTable.listAll();
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        }
        return categories;
    }

    private int getCategoryIdByName(String name) {
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category.getCategory_id();
            }
        }
        try {
            categoryTable.create(new Category(name));
        } catch (SQLException e) {
            Alertmessage.showAlert("Error", "Failed to create category");
            return -1; // Return -1 in case of an error
        } catch (Exception e) {
            Alertmessage.showAlert("Error", "Failed to create category");
            return -1; 
        }
        categories = getCategories();
        return getCategoryIdByName(name); 
    }

}