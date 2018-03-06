package cse417;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.text.DateFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Parses CSV (text) file into a sequence of rows. Each row is returned as an
 * array of strings. However, the parser can optionaly check that particular
 * columns have an expected format: integer, floating-point, or regex.
 */
// TODO(future): support java.util.Pattern as well
public class CsvParser implements Iterator<String[]> {

  /** Reader of the input text file. */
  private final BufferedReader input;

  /** The next line of text from the file or null if none. */
  private String nextLine;

  /**
   * Null or an array of the types of the columns, which can be String.class,
   * Integer.class, Float.class, or DateFormat. Null means no constraints.
   */
  private final Object[] colTypes;

  /**
   * Creates a parser of the given input.
   * @param reader Reader for the input to parse as a CSV.
   * @param hasHeader Whether the CVS starts with a header row, which should be
   *   ignored.
   * @param colTypes If non-null, an array of entries indicating the allowed
   *   values in each column. Each entry can be String.class (arbitrary),
   *   Integer.class (parsable by Integer.parseInt), Float.class (parsable by
   *   Float.parseFloat), or DateFormat (parsable by DateFormat.parse).
   */
  public CsvParser(
      Reader reader, boolean hasHeader, Object[] colTypes) throws IOException {
    this.input = new BufferedReader(reader);
    if (hasHeader)
      this.input.readLine();
    this.nextLine = this.input.readLine();

    this.colTypes = (colTypes != null) ? colTypes.clone() : null;
  }

  /** As above, but only fixes the number of columns not their types. */
  public CsvParser(
      Reader reader, boolean hasHeader, int numCols) throws IOException {
    this(reader, hasHeader, makeStringColTypes(numCols));
  }

  /** As above but with all columns as arbitrary strings. */
  public CsvParser(Reader reader, boolean hasHeader) throws IOException {
    this(reader, hasHeader, null);
  }

  /** As above but with no header and all columns as arbitrary strings. */
  public CsvParser(Reader reader) throws IOException {
    this(reader, false, null);
  }

  /** As above but takes a file name rather than the reader of the file. */
  public CsvParser(String fileName, boolean hasHeader, Object[] colTypes)
      throws IOException {
    this(new FileReader(fileName), hasHeader, colTypes);
  }

  /** As above but only constraints the number of columns not their types. */
  public CsvParser(String fileName, boolean hasHeader, int numCols)
      throws IOException {
    this(fileName, hasHeader, makeStringColTypes(numCols));
  }

  /** As above but with all columns as arbitrary strings. */
  public CsvParser(String fileName, boolean hasHeader)
      throws IOException {
    this(fileName, hasHeader, null);
  }

  /** As above but with no header and all columns as arbitrary strings. */
  public CsvParser(String fileName) throws IOException {
    this(fileName, false, null);
  }

  @Override
  public boolean hasNext() {
    return nextLine != null;
  }

  @Override
  public String[] next() {
    if (nextLine == null)
      throw new NoSuchElementException();
    String[] cols = parseRow(nextLine);

    if (colTypes != null) {
      if (cols.length != colTypes.length) {
        throw new RuntimeException(String.format("CSV: expected %d columns: %s",
            colTypes.length, nextLine));
      }
      for (int i = 0; i < cols.length; i++) {
        if (colTypes[i] == String.class) {
          // anything allowed
        } else if (colTypes[i] == Integer.class) {
          try { Integer.parseInt(cols[i]); }
          catch (NumberFormatException ex) {
            throw new RuntimeException(String.format(
                "CSV: expecting an integer in column %d: %s", i+1, nextLine));
          }
        } else if (colTypes[i] == Float.class) {
          try { Float.parseFloat(cols[i]); }
          catch (NumberFormatException ex) {
            throw new RuntimeException(String.format(
                "CSV: expecting a float in column %d: %s", i+1, nextLine));
          }
        } else if (colTypes[i] instanceof DateFormat) {
          try { ((DateFormat) colTypes[i]).parse(cols[i]); }
          catch (ParseException ex) {
            throw new RuntimeException(String.format(
                "CSV: expecting a date (%s) in column %d: %s", i+1,
                colTypes[i], nextLine));
          }
        } else {
          throw new AssertionError(
              "Unsupported column type: " + colTypes[i].getClass().getName());
        }
      }
    }

    // Grab the next line of input to maintain the invariant of nextLine.
    // We need to wrap any IO exception in a runtime exception to fit the
    // interface of Iterator, unfortunately.
    try { this.nextLine = this.input.readLine(); }
    catch (IOException ex) { throw new RuntimeException(ex); }

    return cols;
  }

  @Override
  public void remove() { throw new UnsupportedOperationException("remove"); }

  /** Returns an array of the given number of string column types. */
  private static Object[] makeStringColTypes(int len) {
    Object[] colTypes = new Object[len];
    Arrays.fill(colTypes, String.class);
    return colTypes;
  }

  /** Returns the columns in the given row. */
  private static String[] parseRow(String row) {
    List<String> cols = new ArrayList<String>();
    int index = 0;

    // Simplify things below by removing the newline.
    if (row.endsWith("\r\n"))
      row = row.substring(0, row.length() - 2);
    else if (row.endsWith("\n"))
      row = row.substring(0, row.length() - 1);

    // Inv: index at the beginning of a column, all previous parsed into cols
    while (index < row.length()) {
      int end;

      if (row.charAt(index) != '"') {
        end = index;
        while (end < row.length() && row.charAt(end) != ',')
          end++;
        cols.add(row.substring(index, end));

      } else {
        StringBuilder buf = new StringBuilder();
        end = index + 1;  // skip opening quote

        while (end < row.length()) {
          if (row.charAt(end) != '"') {
            buf.append(row.charAt(end));
            end += 1;
          } else if (end+1 < row.length() && row.charAt(end+1) == '"') {
            buf.append('"');
            end += 2;
          } else {
            break;
          }
        }

        if (end == row.length()) {
          throw new RuntimeException(
              "CSV: end of line inside of a quoted column: " + row);
        }
        cols.add(buf.toString());

        end++;
        if (end < row.length() && row.charAt(end) != ',') {
          throw new RuntimeException(
              "CSV: quote ends before the end of a quoted column: " + row);
        }
      }

      // end = row.length or row[end] == ','
      index = (end == row.length()) ? end : end + 1;  // skip ','
    }

    return cols.toArray(new String[cols.size()]);
  }
}
