package io.skullabs.tools.agent;

/**
 *
 */
public class Main {

	public static void main( String[] args ) throws InterruptedException {
		new MyClass().sayHello();
	}
}

class MyClass {

	void sayHello(){
		System.out.println( ">>> HELLO!" );
	}
}