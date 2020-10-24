package edu.ycp.cs320.lab02a_rdoster.model;

// model class for Numbers.java
// only the controller should be allowed to call the set methods
// the JSP will call the "get" and "is" methods implicitly
// when the JSP specifies game.first, that gets converted to
//    a call to model.getFirst()
// when the JSP specifies if(game.second), that gets converted to
//    a call to model.getSecond()
public class Numbers {
	private double first, second, third;
	
	public Numbers() {
	}
	
	public void setFirst(double first) {
		this.first = first;
	}
	
	public double getFirst() {
		return first;
	}
	
	public void setSecond(double second) {
		this.second = second;
	}
	
	public double getSecond() {
		return second;
	}
	
	public void setThird(double third) {
		this.third = third;
	}
	
	public double getThird() {
		return third;
	}

	public double setSum() {
		return this.first + this.second + this.third;
	}

	public double getSum(double sum) {
		return sum;
	}

	public double setProd() {
		return this.first * this.second;
	}

	public Double getProd(double product) {
		return product;
	}
	
}
