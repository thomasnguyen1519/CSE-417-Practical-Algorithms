package cse417;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Utility class for parsing command-line arguments. The allowed optional
 * arguments are registered via calls to {@code addOption}. Then, a call to
 * {@code parseArgs} will parse the actual arguments. It returns the
 * non-optimal arguments that were discovered.
 * <p>
 * Optional arguments are those of the form {@code --X} or {@code --X=Y}.
 */
public class ArgParser {

  /** Stores the supported types of options. */
  private static Set<Class<?>> ALLOWED_TYPES = new HashSet<Class<?>>();
  static {
    ALLOWED_TYPES.add(Boolean.class);
    ALLOWED_TYPES.add(Integer.class);
    ALLOWED_TYPES.add(Double.class);
    ALLOWED_TYPES.add(String.class);
  }

  /** Stores the name of the program. */
  private final String programName;

  /** Stores the registered optional arguments and their types. */
  private final Map<String, Class<?>> optionTypes;

  /** Stores the values of any optional arguments seen or null before parse. */
  private Map<String, Object> optionValues;

  /** Creates a parser with no optimal arguments, initially. */
  public ArgParser(String programName) {
    this.programName = programName;
    this.optionTypes = new HashMap<String, Class<?>>();
  }

  /**
   * Registers an optional argument that takes a value of the given type,
   * unless {@code clazz == Boolean.class}, in which case it takes none. The
   * provided class must be {@code Boolean.class}, {@code Integer.class},
   * {@code Double.class}, or {@code String.class}.
   */
  public void addOption(String name, Class<?> clazz) {
    assert !optionTypes.containsKey(name);
    assert ALLOWED_TYPES.contains(clazz);
    optionTypes.put(name, clazz);
  }

  /**
   * Parses the provided arguments, recording the optional ones and returning
   * the non-optional ones.
   */
  public String[] parseArgs(String[] allArgs, int minArgs, int maxArgs) {
    assert optionValues == null;
    optionValues = new HashMap<String, Object>();

    List<String> arguments = new ArrayList<String>();
    for (int i = 0; i < allArgs.length; i++) {
      if (allArgs[i].startsWith("--")) {
        int index = allArgs[i].indexOf('=');
        if (index > 0) {
          String name = allArgs[i].substring(2, index);
          String value = allArgs[i].substring(index + 1);
          if (!optionTypes.containsKey(name))
            usage(minArgs, maxArgs);
          if (optionTypes.get(name) == Boolean.class) {
            usage(minArgs, maxArgs);  // no value allowed
          } else if (optionTypes.get(name) == Integer.class) {
            try { optionValues.put(name, Integer.parseInt(value)); }
            catch (NumberFormatException ex) { usage(minArgs, maxArgs); }
          } else if (optionTypes.get(name) == Double.class) {
            try { optionValues.put(name, Double.parseDouble(value)); }
            catch (NumberFormatException ex) { usage(minArgs, maxArgs); }
          } else if (optionTypes.get(name) == String.class) {
            optionValues.put(name, value);
          } else {
            assert false : "impossible";
         }
        } else if (optionTypes.get(allArgs[i].substring(2)) == Boolean.class) {
          optionValues.put(allArgs[i].substring(2), Boolean.TRUE);
        } else {
          usage(minArgs, maxArgs);
        }
      } else {
        arguments.add(allArgs[i]);
      }
    }

    if (arguments.size() < minArgs || maxArgs < arguments.size())
      usage(minArgs, maxArgs);
    return arguments.toArray(new String[arguments.size()]);
  }

  /** Determines whether the given option was found when parsing. */
  public boolean hasOption(String name) {
    assert optionValues != null;
    return optionValues.containsKey(name);
  }

  /** Returns the value of integer option found during parsing. */
  public int getIntegerOption(String name) {
    assert optionTypes.get(name) == Integer.class;
    assert optionValues.containsKey(name);
    return (Integer) optionValues.get(name);
  }

  /** Returns the value of double option found during parsing. */
  public double getDoubleOption(String name) {
    assert optionTypes.get(name) == Double.class;
    assert optionValues.containsKey(name);
    return (Double) optionValues.get(name);
  }

  /** Returns the value of string option found during parsing. */
  public String getStringOption(String name) {
    assert optionTypes.get(name) == String.class;
    assert optionValues.containsKey(name);
    return (String) optionValues.get(name);
  }

  /** Prints out a usage message and exits. */
  private void usage(int minArgs, int maxArgs) {
    StringBuilder options = new StringBuilder();
    for (String name : optionTypes.keySet()) {
      if (options.length() > 0)
        options.append(' ');

      options.append("--");
      options.append(name);
      if (optionTypes.get(name) == Boolean.class) {
        // nothing
      } else if (optionTypes.get(name) == Integer.class) {
        options.append("=<int>");
      } else if (optionTypes.get(name) == Double.class) {
        options.append("=<double>");
      } else if (optionTypes.get(name) == String.class) {
        options.append("=..");
      } else {
        assert false : "impossible";
      }
    }

    System.err.printf("Usage: %s %s ... (+%s arguments)\n", programName,
        options.toString(), minArgs, maxArgs,
        (minArgs == maxArgs) ? String.format("%d", minArgs) :
            String.format("%d-%d", minArgs, maxArgs));
    System.exit(1);
  }
}
