package at.tuwien.ict.miklas.mind.aconamind.mind;

import java.io.PrintWriter;
import java.io.Reader;

public abstract class TextDevice {
  public abstract TextDevice printf(String fmt, Object... params) throws Exception;
  public abstract String readLine() throws Exception;
  public abstract char[] readPassword() throws Exception;
  public abstract Reader reader() throws Exception;
  public abstract PrintWriter writer() throws Exception;
}