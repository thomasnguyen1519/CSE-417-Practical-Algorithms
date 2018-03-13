package cse417;

import java.io.*;
import java.text.*;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;


public class CsvParserTest {

  @Test public void testEmpty() {
    List<String[]> rows = parseRows("");
    assertEquals(0, rows.size());
  }

  @Test public void testSingleColumn() {
    List<String[]> rows = parseRows("a\nb\nc");
    assertEquals(3, rows.size());
    assertArrayEquals(new String[] {"a"}, rows.get(0));
    assertArrayEquals(new String[] {"b"}, rows.get(1));
    assertArrayEquals(new String[] {"c"}, rows.get(2));
  }

  @Test public void testSingleRow() {
    List<String[]> rows = parseRows("a,b,c");
    assertEquals(1, rows.size());
    assertArrayEquals(new String[] {"a", "b", "c"}, rows.get(0));

    rows = parseRows("A,B,C\na,b,c", true, null);
    assertEquals(1, rows.size());
    assertArrayEquals(new String[] {"a", "b", "c"}, rows.get(0));
  }

  @Test public void testMultipleRows() {
    List<String[]> rows = parseRows("a,b,c\nd,e,f\ng,h,i");
    assertEquals(3, rows.size());
    assertArrayEquals(new String[] {"a", "b", "c"}, rows.get(0));
    assertArrayEquals(new String[] {"d", "e", "f"}, rows.get(1));
    assertArrayEquals(new String[] {"g", "h", "i"}, rows.get(2));

    List<String[]> rows2 = parseRows("a,b,c\r\nd,e,f\r\ng,h,i\r\n");
    assertEquals(3, rows2.size());
    assertArrayEquals(new String[] {"a", "b", "c"}, rows2.get(0));
    assertArrayEquals(new String[] {"d", "e", "f"}, rows2.get(1));
    assertArrayEquals(new String[] {"g", "h", "i"}, rows2.get(2));
  }

  @Test public void testQuotes() {
    List<String[]> rows = parseRows("\"a\",\"\"\"b\"\"\",\"c\"");
    assertEquals(1, rows.size());
    assertArrayEquals(new String[] {"a", "\"b\"", "c"}, rows.get(0));
  }

  @Test public void testBadQuote() {
    try { parseRows("a,\"b,c"); fail(); } // no end
    catch (RuntimeException ex) { /* pass */ }

    try { parseRows("a,\"b\" ,c"); fail(); } // extra space
    catch (RuntimeException ex) { /* pass */ }
  }

  /** Format for the dates used in the data files. */
  private static final DateFormat DATE_FORMAT =
      new SimpleDateFormat("dd-MMM-yy");

  @Test public void testWithTypes() {
    List<String[]> rows = parseRows(
        "abc,2,3.14,01-Jan-78\ndef,-3,\".25\",05-Dec-87",
        false,
        new Object[] { String.class, Integer.class, Float.class, DATE_FORMAT});
    assertEquals(2, rows.size());
    assertArrayEquals(new String[] {"abc", "2", "3.14", "01-Jan-78"},
        rows.get(0));
    assertArrayEquals(new String[] {"def", "-3", ".25", "05-Dec-87"},
        rows.get(1));
  }

  @Test public void testBadTypes() {
    try { parseRows("3.14", false, new Object[] { Integer.class }); fail(); }
    catch (RuntimeException ex) { /* pass */ }

    try { parseRows("abc", false, new Object[] { Float.class }); fail(); }
    catch (RuntimeException ex) { /* pass */ }

    try {
      parseRows("01-Fredcember-78", false, new Object[] { DATE_FORMAT });
      fail();
    } catch (RuntimeException ex) { /* pass */ }
  }

  /** Returns the rows produced by parsing the given content as a CSV. */
  private List<String[]> parseRows(String content) {
    return parseRows(content, false, null);
  }

  /**
   * Returns the rows produced by parsing the given content as a CSV.
   * @param content Content of the CSV file to parse.
   * @param hasHeader Indicates whether the first line is a header to skip.
   * @param colTypes If non-null, constraints on the types of the columns.
   */
  private List<String[]> parseRows(
        String content, boolean hasHeader, Object[] colTypes) {
    List<String[]> rows = new ArrayList<String[]>();
    try {
      Reader reader = new StringReader(content);
      CsvParser parser = new CsvParser(reader, hasHeader, colTypes);
      while (parser.hasNext())
        rows.add(parser.next());
    } catch (IOException ex) {
      throw new RuntimeException(ex);  // should not happen here
    }
    return rows;
  }
}
