package at.tuwien.ict.miklas.mind.aconamind.restserver;

public class FuckingSingletonHack {
	private static JerseyRestServer function = null;
	
	public static void setFunction(JerseyRestServer function) {
		FuckingSingletonHack.function = function;
	}
	
	public static JerseyRestServer getFunction() {
		return function;
	}
}
