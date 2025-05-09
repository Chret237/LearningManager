package com.example.librarymanager.Database;

import com.example.librarymanager.Models.Loan;
import com.example.librarymanager.utils.DatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoanTable implements Repository<Loan> {
  private static final String INSERT_LOAN = "INSERT INTO loans (book_id, user_id, due_at) VALUES (?, ?, ?)";
  private static final String UPDATE_LOAN = "UPDATE loans SET status = ?, returned_at = ? WHERE loan_id = ?";
  private static final String DELETE_LOAN = "DELETE FROM loans WHERE loan_id = ?";
  private static final String SELECT_ALL_LOANS = "SELECT * FROM loans";

  @Override
  public void create(Loan loan) throws SQLException {
    try (Connection conn = DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(INSERT_LOAN)) {
      stmt.setInt(1, loan.getBookId());
      stmt.setInt(2, loan.getUserId());
      
      if (loan.getDueAt() == null) {
        throw new SQLException("Due date cannot be null");
      }
      
      String formattedDate = loan.getDueAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " ");
      stmt.setString(3, formattedDate);
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Loan-create:"+e);
      throw e;  
    }
  }

  @Override
  public void Update(Loan loan) throws SQLException {
    try (Connection conn = DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(UPDATE_LOAN)) {
      stmt.setString(1, loan.getStatus());
      
      if (loan.getReturnedAt() == null) {
        throw new SQLException("Returned date cannot be null");
      }
      
      String formattedDate = loan.getReturnedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " ");
      stmt.setString(2, formattedDate);
      stmt.setInt(3, loan.getLoanId());
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Loan-update:"+e);
      throw e;  
    }
  }

  @Override
  public void Delete(int id) throws SQLException {
    try (Connection conn = DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(DELETE_LOAN)) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Loan-delete:"+e);
      throw e;  
    }
  }

  @Override
  public List<Loan> listAll() throws SQLException {
    List<Loan> loans = new ArrayList<>();
    try (Connection conn = DatabaseUtils.getConnection();
        PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_LOANS);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        Loan loan = new Loan();
        loan.setLoanId(rs.getInt("loan_id"));
        loan.setBookId(rs.getInt("book_id"));
        loan.setUserId(rs.getInt("user_id"));
        
        String borrowedDate = rs.getString("borrowed_at");
        if (borrowedDate != null && !borrowedDate.isEmpty()) {
          loan.setBorrowedAt(LocalDateTime.parse(borrowedDate.replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        String dueAtStr = rs.getString("due_at");
        if (dueAtStr != null && !dueAtStr.isEmpty()) {
          loan.setDueAt(LocalDateTime.parse(dueAtStr.replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        String returnedAtStr = rs.getString("returned_at");
        if (returnedAtStr != null && !returnedAtStr.isEmpty()) {
          loan.setReturnedAt(LocalDateTime.parse(returnedAtStr.replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        loan.setStatus(rs.getString("status"));
        loans.add(loan);
      }
    } catch (SQLException e) {
      System.err.println("Loan-listAll:"+e);
      throw e;  
    }
    return loans;
  }
}
