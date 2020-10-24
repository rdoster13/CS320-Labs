package edu.ycp.cs320.booksdb.persist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.ycp.cs320.booksdb.model.Author;
import edu.ycp.cs320.booksdb.model.Book;
import edu.ycp.cs320.booksdb.model.Pair;
import edu.ycp.cs320.sqldemo.DBUtil;

public class DerbyDatabase implements IDatabase {
	static {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (Exception e) {
			throw new IllegalStateException("Could not load Derby driver");
		}
	}
	
	private interface Transaction<ResultType> {
		public ResultType execute(Connection conn) throws SQLException;
	}

	private static final int MAX_ATTEMPTS = 10;

	@Override
	public List<Pair<Author, Book>> findAuthorAndBookByTitle(final String title) {
		return executeTransaction(new Transaction<List<Pair<Author,Book>>>() {
			@Override
			public List<Pair<Author, Book>> execute(Connection conn) throws SQLException {
				PreparedStatement stmt = null;
				ResultSet resultSet = null;
				
				try {
					// retreive all attributes from both Books and Authors tables
					stmt = conn.prepareStatement(
							"select authors.*, books.* " +
							"  from authors, books " +
							" where authors.author_id = books.author_id " +
							"   and books.title = ?"
					);
					stmt.setString(1, title);
					
					List<Pair<Author, Book>> result = new ArrayList<Pair<Author,Book>>();
					
					resultSet = stmt.executeQuery();
					
					// for testing that a result was returned
					Boolean found = false;
					
					while (resultSet.next()) {
						found = true;
						
						// create new Author object
						// retrieve attributes from resultSet starting with index 1
						Author author = new Author();
						loadAuthor(author, resultSet, 1);
						
						// create new Book object
						// retrieve attributes from resultSet starting at index 4
						Book book = new Book();
						loadBook(book, resultSet, 4);
						
						result.add(new Pair<Author, Book>(author, book));
					}
					
					// check if the title was found
					if (!found) {
						System.out.println("<" + title + "> was not found in the books table");
					}
					
					return result;
				} finally {
					DBUtil.closeQuietly(resultSet);
					DBUtil.closeQuietly(stmt);
				}
			}
		});
	}
	

	@Override
	public List<Pair<Author, Book>> findAuthorAndBookByAuthorLastName(String lastname) {
		return executeTransaction(new Transaction<List<Pair<Author,Book>>>() {
			@Override
			public List<Pair<Author, Book>> execute(Connection conn) throws SQLException {
				PreparedStatement stmt = null;
				ResultSet resultSet = null;
				
				try {
					// Retrieve all attributes from both Books and Authors tables based on last name
					stmt = conn.prepareStatement(
							"select authors.*, books.* " +
							"  from authors, books " +
							" where authors.author_id = books.author_id " +
							"   and authors.lastname = ?"
					);
					stmt.setString(1, lastname);
					
					List<Pair<Author, Book>> result = new ArrayList<Pair<Author,Book>>();
					
					resultSet = stmt.executeQuery();
					
					// for testing that a result was returned
					Boolean found = false;
					
					while (resultSet.next()) {
						found = true;
						
						// create new Author object
						// retrieve attributes from resultSet starting with index 1
						Author author = new Author();
						loadAuthor(author, resultSet, 1);
						
						// create new Book object
						// retrieve attributes from resultSet starting at index 4
						Book book = new Book();
						loadBook(book, resultSet, 4);
						
						result.add(new Pair<Author, Book>(author, book));
					}
					
					// check if the author was found
					if (!found) {
						System.out.println("<" + lastname + "> was not found in the books table");
					}
					
					return result;
				} finally {
					DBUtil.closeQuietly(resultSet);
					DBUtil.closeQuietly(stmt);
				}
			}
		});	
	}
	
	@Override
	public List<Pair<Author, Book>> insertNewBookWithAuthor(String lastname, String firstname, String title, String ISBN, Integer published) {
		return executeTransaction(new Transaction<List<Pair<Author,Book>>>() {
			@Override
			public List<Pair<Author, Book>> execute(Connection conn) throws SQLException {
				PreparedStatement stmt = null;
				ResultSet resultSet = null;
				
				List<Pair<Author, Book>> result = new ArrayList<Pair<Author,Book>>();
				
				// PreparedStatement references - one for each of the queries / inserts 
				PreparedStatement stmtAuthorID1    = null;
				PreparedStatement stmtAuthorID2    = null;		
				PreparedStatement stmtInsertAuthor = null;
				PreparedStatement stmtInsertBook   = null;
				PreparedStatement stmtCheckResults = null;
				
				// ResultSet references - one for each query
				ResultSet resultSetAuthorID1    = null;
				ResultSet resultSetAuthorID2    = null;
						

				// now connect to the database
				// the DB name is "test.db"
				// in this case, it is expected to be located at the same level as the project sub-folders
				// create the DB, if it doesn't already exist - but that won't create the schema, or populate the tables
				//you can do that by running DerbyDatabase.java as an Application
				conn = DriverManager.getConnection("jdbc:derby:test.db;create=true");

				@SuppressWarnings("resource")
				Scanner keyboard = new Scanner(System.in);

				// prompt user for all information for new book
				// this could include a new author
				try {
					
					// to insert the new book, we have to have an author ID
					// first check if the author exists, and if so, retrieve their author_ID
					// if this is a book by a new author, insert the author into AUTHORS table first
					// then, retrieve their author ID (auto-generated for AUTHORS table by the DB)
					// finally, now that we have the author ID (for an existing or new author),
					// insert the new book, with that author ID (it is a foreign key in the BOOKS table)
					stmtAuthorID1 = conn.prepareStatement(
							"select authors.author_id "
							+ "  from authors "
							+ "  where authors.firstname = ? and authors.lastname = ? "
					);

					// substitute the author's first name entered by the user for the 1st placeholder
					stmtAuthorID1.setString(1, firstname);

					// substitute the author's last name entered by the user for the 2nd placeholder
					stmtAuthorID1.setString(2, lastname);
					
					// execute the query
					resultSetAuthorID1 = stmtAuthorID1.executeQuery();

					// we'll use this to save the retrieved author ID (initialize to an out-of-range value)
					int authorID = -1;
					
					// check if an author ID was returned from the query
					if (resultSetAuthorID1.next()) {
						
						// found the author
						// get their author ID from the 1st (and only) attribute, index 1
						// authorID is an integer, retrieve it as an Integer from the result set, first index
						authorID = resultSetAuthorID1.getInt(1);
						
						System.out.println("Existing author found in AUTHORS table\n");
					}
					else
					{
						// new author, need to insert into the AUTHORS table
						System.out.println("Inserting new author into AUTHORS table\n");
						
						// otherwise, insert new author into the AUTHORS table
						stmtInsertAuthor = conn.prepareStatement(
								"insert into authors (lastname, firstname) "
								+ "values (?, ?)"
						);
						
						// substitute the author's last name entered by the user for the 1st placeholder
						stmtInsertAuthor.setString(1, lastname);
						
						// substitute the author's last name entered by the user for the 2nd placeholder
						stmtInsertAuthor.setString(2, firstname);				
						
						// execute the DB update (this is not a query, but a change to the AUTHORS table in the DB)
						stmtInsertAuthor.executeUpdate();

						// the author should now exist, retrieve author_ID
						// it was auto-generated by the DB as a primary key in the AUTHORS table
						stmtAuthorID2 = conn.prepareStatement(
								"select authors.author_id "
										+ "  from authors "
										+ "  where authors.firstname = ? and authors.lastname = ? "
						);

						// substitute the author's first name entered by the user for the 1st placeholder
						stmtAuthorID2.setString(1, firstname);

						// substitute the author's last name entered by the user for the 2nd placeholder
						stmtAuthorID2.setString(2, lastname);
						
						// execute the query
						resultSetAuthorID2 = stmtAuthorID2.executeQuery();

						// check if an author was returned from the query
						if (resultSetAuthorID2.next()) {
							
							// get new author's ID (as an Integer) from the first (and only) attribute, index 1
							authorID = resultSetAuthorID2.getInt(1);
							
							System.out.println("New author inserted into AUTHORS table with author_ID: " + authorID + "\n");
						}
						else {
							
							// we really shouldn't ever hit this, and it could be handled as an exception
							System.out.println("Something very bad has happened - the new author was not found in the AUTHORS table");
						}
					}
					
					// now we can insert the new book into the BOOKS table
					stmtInsertBook = conn.prepareStatement(
							"insert into books (author_id, title, ISBN, published) "
							+ "  values (?, ?, ?, ?)"
					);
					
					// substitute the author's author ID for the 1st placeholder (as in Integer)
					stmtInsertBook.setInt(1, authorID);
					
					// substitute the book title entered by the user for the 2nd placeholder (as a String)
					stmtInsertBook.setString(2, title);
					
					// substitute the book's ISBN entered by the user for the 3rd placeholder (as a String)
					stmtInsertBook.setString(3, ISBN);
					
					// substitute the book's publish year entered by the use for the 4th placeholder (as an Integer)
					stmtInsertBook.setInt(4, published);
					
					// execute the DB update (this is not a query, but a change to the BOOKS table in the DB)
					stmtInsertBook.executeUpdate();
					
					System.out.println("New book inserted into BOOKS table with title <" + title + 
							           "> for author " + firstname + " " + lastname + "\n");			
					// Retrieve all attributes from both Books and Authors tables based on last name
					stmt = conn.prepareStatement(
							"select authors.*, books.* " +
							"  from authors, books " +
							" where authors.author_id = books.author_id " +
							"   and authors.lastname = ? and authors.firstname = ?"
					);
					stmt.setString(1, lastname);
					stmt.setString(2, firstname);
					
					resultSet = stmt.executeQuery();
					
					// for testing that a result was returned
					Boolean found = false;
					
					while (resultSet.next()) {
						found = true;
						
						// create new Author object
						// retrieve attributes from resultSet starting with index 1
						Author author = new Author();
						loadAuthor(author, resultSet, 1);
						
						// create new Book object
						// retrieve attributes from resultSet starting at index 4
						Book book = new Book();
						loadBook(book, resultSet, 4);
						
						result.add(new Pair<Author, Book>(author, book));
					}
					
					// check if the author was found
					if (!found) {
						System.out.println("<" + lastname + "> was not found in the books table");
					}
					
					return result;
					
				} finally {
					
					
					// close ResultSets
					DBUtil.closeQuietly(resultSetAuthorID1);
					DBUtil.closeQuietly(resultSetAuthorID2);
					
					// close PreparedStatements
					DBUtil.closeQuietly(stmtAuthorID1);
					DBUtil.closeQuietly(stmtAuthorID2);		
					DBUtil.closeQuietly(stmtInsertAuthor);
					DBUtil.closeQuietly(stmtInsertBook);
					DBUtil.closeQuietly(stmtCheckResults);
					
					// close DB Connection
					DBUtil.closeQuietly(conn);

					// close Keyboard
					keyboard.close();
				}
			}
		});
	} 
	
	/*
	@Override
	public void insertAuthor(String lastName, String firstName) {
		throw new UnsupportedOperationException("Not Implemented yet");
	}
	
	
	@Override
	public List<Pair<Author, Book>> insertNewBookWithAuthor(String title, String ISBN, String published) {
		throw new UnsupportedOperationException("Not Implemented yet");
	}
	*/

	public<ResultType> ResultType executeTransaction(Transaction<ResultType> txn) {
		try {
			return doExecuteTransaction(txn);
		} catch (SQLException e) {
			throw new PersistenceException("Transaction failed", e);
		}
	}
	
	public<ResultType> ResultType doExecuteTransaction(Transaction<ResultType> txn) throws SQLException {
		Connection conn = connect();
		
		try {
			int numAttempts = 0;
			boolean success = false;
			ResultType result = null;
			
			while (!success && numAttempts < MAX_ATTEMPTS) {
				try {
					result = txn.execute(conn);
					conn.commit();
					success = true;
				} catch (SQLException e) {
					if (e.getSQLState() != null && e.getSQLState().equals("41000")) {
						// Deadlock: retry (unless max retry count has been reached)
						numAttempts++;
					} else {
						// Some other kind of SQLException
						throw e;
					}
				}
			}
			
			if (!success) {
				throw new SQLException("Transaction failed (too many retries)");
			}
			
			// Success!
			return result;
		} finally {
			DBUtil.closeQuietly(conn);
		}
	}

	private Connection connect() throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:derby:test.db;create=true");
		
		// Set autocommit to false to allow execution of
		// multiple queries/statements as part of the same transaction.
		conn.setAutoCommit(false);
		
		return conn;
	}
	
	private void loadAuthor(Author author, ResultSet resultSet, int index) throws SQLException {
		author.setAuthorId(resultSet.getInt(index++));
		author.setLastname(resultSet.getString(index++));
		author.setFirstname(resultSet.getString(index++));
	}
	
	private void loadBook(Book book, ResultSet resultSet, int index) throws SQLException {
		book.setBookId(resultSet.getInt(index++));
		book.setAuthorId(resultSet.getInt(index++));
		book.setTitle(resultSet.getString(index++));
		book.setIsbn(resultSet.getString(index++));
		book.setPublished(resultSet.getInt(index++));		
	}
	
	public void createTables() {
		executeTransaction(new Transaction<Boolean>() {
			@Override
			public Boolean execute(Connection conn) throws SQLException {
				PreparedStatement stmt1 = null;
				PreparedStatement stmt2 = null;
				
				try {
					stmt1 = conn.prepareStatement(
						"create table authors (" +
						"	author_id integer primary key " +
						"		generated always as identity (start with 1, increment by 1), " +									
						"	lastname varchar(40)," +
						"	firstname varchar(40)" +
						")"
					);	
					stmt1.executeUpdate();
					
					stmt2 = conn.prepareStatement(
							"create table books (" +
							"	book_id integer primary key " +
							"		generated always as identity (start with 1, increment by 1), " +
							"	author_id integer constraint author_id references authors, " +
							"	title varchar(70)," +
							"	isbn varchar(15)," +
							"   published integer " +
							")"
					);
					stmt2.executeUpdate();
					
					return true;
				} finally {
					DBUtil.closeQuietly(stmt1);
					DBUtil.closeQuietly(stmt2);
				}
			}
		});
	}
	
	public void loadInitialData() {
		executeTransaction(new Transaction<Boolean>() {
			@Override
			public Boolean execute(Connection conn) throws SQLException {
				List<Author> authorList;
				List<Book> bookList;
				
				try {
					authorList = InitialData.getAuthors();
					bookList = InitialData.getBooks();
				} catch (IOException e) {
					throw new SQLException("Couldn't read initial data", e);
				}

				PreparedStatement insertAuthor = null;
				PreparedStatement insertBook   = null;

				try {
					// populate authors table (do authors first, since author_id is foreign key in books table)
					insertAuthor = conn.prepareStatement("insert into authors (lastname, firstname) values (?, ?)");
					for (Author author : authorList) {
//						insertAuthor.setInt(1, author.getAuthorId());	// auto-generated primary key, don't insert this
						insertAuthor.setString(1, author.getLastname());
						insertAuthor.setString(2, author.getFirstname());
						insertAuthor.addBatch();
					}
					insertAuthor.executeBatch();
					
					// populate books table (do this after authors table,
					// since author_id must exist in authors table before inserting book)
					insertBook = conn.prepareStatement("insert into books (author_id, title, isbn, published) values (?, ?, ?, ?)");
					for (Book book : bookList) {
//						insertBook.setInt(1, book.getBookId());		// auto-generated primary key, don't insert this
						insertBook.setInt(1, book.getAuthorId());
						insertBook.setString(2, book.getTitle());
						insertBook.setString(3, book.getIsbn());
						insertBook.setInt(4,  book.getPublished());
						insertBook.addBatch();
					}
					insertBook.executeBatch();
					
					return true;
				} finally {
					DBUtil.closeQuietly(insertBook);
					DBUtil.closeQuietly(insertAuthor);
				}
			}
		});
	}
	
	// The main method creates the database tables and loads the initial data.
	public static void main(String[] args) throws IOException {
		System.out.println("Creating tables...");
		DerbyDatabase db = new DerbyDatabase();
		db.createTables();
		
		System.out.println("Loading initial data...");
		db.loadInitialData();
		
		System.out.println("Success!");
	}


	

}
