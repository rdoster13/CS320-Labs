package edu.ycp.cs320.lab02a_rdoster.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.ycp.cs320.lab02a_rdoster.model.GuessingGame;

public class GuessingGameTest {
	private GuessingGame model;
	
	@Before
	public void setUp() {
		model = new GuessingGame();
	}
	
	@Test
	public void testSetMin() {
		model.setMin(1);
		assertEquals(1, model.getMin());
	}
	
	@Test
	public void testSetMax() {
		model.setMax(100);
		assertEquals(100, model.getMax());
	}
	
	@Test
	public void testIsDone() {
		model.isDone();
		assertTrue(model.getMax() == model.getMin());
	}
	
	@Test
	public void testLessThanLowersMax() {
		model.getGuess();
		int firstMaxGuess = model.getGuess();
		model.setIsLessThan(model.getGuess());
		assertTrue(model.getMax() < firstMaxGuess);
	}
	
	@Test
	public void testGreaterThanRaisesMin() {
		model.getGuess();
		int firstMinGuess = model.getGuess();
		model.setIsGreaterThan(model.getGuess());
		assertTrue(model.getMin() > firstMinGuess);
	}
}
