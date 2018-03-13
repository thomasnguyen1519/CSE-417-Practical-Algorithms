package cse417;

import org.junit.Test;
import static org.junit.Assert.*;


public class ArgParserTest {

  @Test
  public void testNoOptions() {
    ArgParser argParser = new ArgParser("test");
    assertArrayEquals(argParser.parseArgs(new String[] {}, 0, 0),
        new String[] {});

    argParser = new ArgParser("test");
    assertArrayEquals(argParser.parseArgs(new String[] {"1"}, 1, 1),
        new String[] {"1"});

    argParser = new ArgParser("test");
    assertArrayEquals(argParser.parseArgs(new String[] {}, 0, 1),
        new String[] {});

    argParser = new ArgParser("test");
    assertArrayEquals(argParser.parseArgs(new String[] {"1", "2", "3"}, 3, 3),
        new String[] {"1", "2", "3"});
  }

  @Test
  public void testAllOptionTypes() {
    ArgParser argParser = new ArgParser("test");
    argParser.addOption("bool", Boolean.class);
    argParser.addOption("int", Integer.class);
    argParser.addOption("double", Double.class);
    argParser.addOption("str", String.class);
    String[] allArgs = new String[] {
        "--bool", "1", "--int=3", "2", "--double=3.0", "--str=foo", "3" };
    assertArrayEquals(argParser.parseArgs(allArgs, 0, 3),
        new String[] {"1", "2", "3"});
    assertTrue(argParser.hasOption("bool"));
    assertEquals(argParser.getIntegerOption("int"), 3);
    assertEquals(argParser.getDoubleOption("double"), 3.0, 1e-10);
    assertEquals(argParser.getStringOption("str"), "foo");
  }
}
