package main;

import controller.Controller;

public class Main {

	public static void main(String[] args) {
		Controller controller = Controller.getInstance();
		controller.init();
	}
}
