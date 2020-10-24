package edu.ycp.cs320.lab02a_rdoster.controller;

import edu.ycp.cs320.lab02a_rdoster.model.Numbers;

public class NumbersController {
	private Numbers model;
	
	public void setModel(Numbers model) {
		this.model = model;
	}
	
	
	public Double setFinalSum(double first, double second, double third) {
		model.setFirst(first);
		model.setSecond(second);
		model.setThird(third);
		double currentSum = model.setSum();
		return model.getSum(currentSum);
	}
	
	public Double setMult(double first, double second) {
		model.setFirst(first);
		model.setSecond(second);
		double currentProd = model.setProd();
		return model.getProd(currentProd);
	}

	
}

