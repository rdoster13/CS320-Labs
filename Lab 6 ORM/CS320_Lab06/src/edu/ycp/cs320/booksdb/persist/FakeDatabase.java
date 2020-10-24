package edu.ycp.cs320.booksdb.persist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import edu.ycp.cs320.booksdb.model.Author;
import edu.ycp.cs320.booksdb.model.Book;
import edu.ycp.cs320.booksdb.model.Pair;

public class FakeDatabase implements IDatabase {
	
	private List<Author> authorList;
	private List<Book> bookList;
	
	public FakeDatabase() {
		authorList = new ArrayList<Author>();
		bookList = new ArrayList<Book>();
		
		// Add initial data
		readInitialData();
		
		System.out.println(authorList.size() + " authors");
		System.out.println(bookList.size() + " books");
	}

	public void readInitialData() {
		try {
			authorList.addAll(InitialData.getAuthors());
			bookList.addAll(InitialData.getBooks());
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't read initial data", e);
		}
	}
	
	@Override
	public List<Pair<Author, Book>> findAuthorAndBookByTitle(String title) {
		List<Pair<Author, Book>> result = new ArrayList<Pair<Author,Book>>();
		for (Book book : bookList) {
			if (book.getTitle().equals(title)) {
				Author author = findAuthorByAuthorId(book.getAuthorId());
				result.add(new Pair<Author, Book>(author, book));
			}
		}
		return result;
	}

	private Author findAuthorByAuthorId(int authorId) {
		for (Author author : authorList) {
			if (author.getAuthorId() == authorId) {
				return author;
			}
		}
		return null;
	}
	
	@Override
	public List<Pair<Author, Book>> findAuthorAndBookByAuthorLastName(String lastname){
	
		List<Pair<Author, Book>> result = new ArrayList<Pair<Author,Book>>();
		// Check the author list for the author(s) with the given last name
		for (Author authorCheck : authorList) {
			if (authorCheck.getLastname().equals(lastname)) {
				// Check the book list for books that match the Author ID given by user
				for (Book book : bookList) {
					// compare book.authorID and authorCheck.authorID, if they match, add it to the result
					if (book.getAuthorId() == authorCheck.getAuthorId()) {
						Author author = findAuthorByAuthorId(book.getAuthorId());
						result.add(new Pair<Author, Book>(author, book));
						// Sort titles in ascending order
						result.sort(bookTitleSort);
					}
				}
			}
		};
		
		return result;
	}
	
	@Override
	public List<Pair<Author, Book>> insertNewBookWithAuthor(String lastname, String firstname, String title, String ISBN, Integer published) {
		// Set AuthorID / BookID out of Bounds so it wont be found by default
		int authorId = -1;
		int bookId   = -1;
		
		//Create new Author / Book to add to lists
		Author newAuthor = new Author();
		Book newBook = new Book();
		
		// Search for the Author, by first and last name, get author_id
		for (Author author : authorList) {
			if (author.getLastname().equals(lastname) && author.getFirstname().equals(firstname)) {
				authorId = author.getAuthorId();
			}
		}
		
		// if not found, add new Author to list
		if (authorId < 0) {
			authorId = authorList.size() + 1;
			// add new Author to list
			newAuthor.setAuthorId(authorId);
			newAuthor.setLastname(lastname);
			newAuthor.setFirstname(firstname);
			authorList.add(newAuthor);
			
			System.out.println("New author:  " + lastname + ", " + firstname + " added successfully");
		}

		bookId = bookList.size() + 1;
		// add new Book to list
		newBook.setBookId(bookId);
		newBook.setAuthorId(authorId);
		newBook.setTitle(title);
		newBook.setIsbn(ISBN);
		newBook.setPublished(published);
		bookList.add(newBook);
		
		// Create a new result to store the added book / author
		List<Pair<Author, Book>> result = new ArrayList<Pair<Author,Book>>();
		result.add(new Pair<Author, Book>(newAuthor, newBook));
		
		// return new Pair
		return result;
	}
	
	/* @Override
	public void insertAuthor(String lastName, String firstName) {
		// Create a new author object to add to table
		Author newAuthor = new Author(); 
			// set the full name of the author
			newAuthor.setLastname(lastName);
			newAuthor.setFirstname(firstName);
			authorList.add(newAuthor);
			System.out.println("Author " + lastName + ", " + firstName + " has been inserted successfully");
	};
	
	
	@Override
	public List<Pair<Author, Book>> insertNewBookWithAuthor(String title, String ISBN, String published) {
		return null;
	}
	 */

	public static Comparator<Pair<Author, Book>> bookTitleSort = new Comparator<Pair<Author, Book>>() {
		
		@Override
		public int compare(Pair<Author, Book> o1, Pair<Author, Book> o2) {
			  // Sort titles in ascending order
			 return o1.getRight().getTitle().compareTo(o2.getRight().getTitle());
		}
	};
}
