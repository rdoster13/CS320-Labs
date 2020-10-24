package edu.ycp.cs320.booksdb;

import java.util.List;
import java.util.Scanner;

import edu.ycp.cs320.booksdb.model.Author;
import edu.ycp.cs320.booksdb.model.Book;
import edu.ycp.cs320.booksdb.model.Pair;
import edu.ycp.cs320.booksdb.persist.DatabaseProvider;
import edu.ycp.cs320.booksdb.persist.IDatabase;

public class InsertNewBookWithAuthor {
	public static void main(String[] args) throws Exception {
		Scanner keyboard = new Scanner(System.in);

		// Create the default IDatabase instance
		InitDatabase.init(keyboard);
		
		System.out.print("Enter an author last name: ");
		String lastName = keyboard.nextLine();
		System.out.print("Enter an author first name: ");
		String firstName = keyboard.nextLine();
		System.out.print("Enter a book title: ");
		String title = keyboard.nextLine();
		System.out.print("Enter an ISBN number for the book: ");
		String ISBN = keyboard.nextLine();
		System.out.print("Enter a publishing year for the book: ");
		Integer published = keyboard.nextInt();
		
		
		// get the DB instance and execute transaction
		IDatabase db = DatabaseProvider.getInstance();
		List<Pair<Author, Book>> authorList = db.insertNewBookWithAuthor(lastName, firstName, title, ISBN, published);
		
		// check if anything was returned and output the list
		if (authorList.isEmpty()) {
			System.out.println("No books found with Author <" + lastName + ", " + firstName + ">");
		}
		else {
			System.out.println("Here is a list of the books from the current author: ");
			for (Pair<Author, Book> authorBook : authorList) {
				Author author = authorBook.getLeft();
				Book book = authorBook.getRight();
				System.out.println(author.getLastname() + "," + author.getFirstname() + "," + book.getTitle() + "," + book.getIsbn() + "," + book.getPublished());
			}
		}
	}
}
