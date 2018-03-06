package cse417;

import java.io.IOException;
import java.io.PrintStream;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Stores a model for how individual teams cause the change in expected points
 * from the starting point of the drive to the ending point. Each such change
 * is predicted to be (1) a constant term plus (2) the parameter for the
 * offense minus (3) the parameter for opposing defense.
 * <p>
 * Two of the important methods of this class allow the evaluation of the
 * <b>loss function</b>. This is a function that computes the error of the
 * predictions made by the model on provided data. It is the sum of two terms.
 * The first is the sum of the squared error on each prediction (i.e., the
 * square of the difference between the actual and predicted change). The
 * second is a constant times the L1 norm of the model, a measure of its
 * complexity. The constant used to multiply the L1 norm is called the penalty.
 * A larger penalty will give better scores to simpler models and worse scores
 * to more complex models. The L1 norm is used because it pushes the optimal
 * model to have parameters that are zero rather than just small.
 */
public class TeamModel {

  /** List of the NFL teams (as of 2016). */
  public static final List<String> TEAMS = Collections.unmodifiableList(
      Arrays.asList(new String[] {
          "ARI", "ATL", "BAL", "BUF", "CAR", "CHI", "CIN", "CLE",
          "DAL", "DEN", "DET", "GB", "HOU", "IND", "JAC", "KC",
          "LA", "MIA", "MIN", "NE", "NO", "NYG", "NYJ", "OAK",
          "PHI", "PIT", "SD", "SEA", "SF", "TB", "TEN", "WAS"
      }));

  /** Stores the value of the constant term in the model. */
  private double constant;

  /** Stores the value of the parameter for each offense. */
  private final Map<String, Double> offenses;

  /** Stores the value of the parameter for each defense. */
  private final Map<String, Double> defenses;

  /** Creates a model with all parameters set to zero. */
  public TeamModel() {
    this.offenses = new HashMap<String, Double>();
    this.defenses = new HashMap<String, Double>();

    for (String team : TEAMS) {
      offenses.put(team, 0.);
      defenses.put(team, 0.);
    }
  }

  /** Creates a model with the given parameters. */
  private TeamModel(double constant, Map<String, Double> offenses,
      Map<String, Double> defenses) {
    this.constant = constant;
    this.offenses = new HashMap<String, Double>(offenses);
    this.defenses = new HashMap<String, Double>(defenses);
  }

  /** Returns a copy of this model. */
  public TeamModel copy() {
    return new TeamModel(constant, offenses, defenses);
  }

  /** Returns the current value of the constant term. */
  public double getConstant() { return constant; }
  
  /** Sets the constant term to the given value. */
  public TeamModel setConstant(double constant) {
    this.constant = constant;
    return this;
  }

  /** Returns the current value of the parameter for the given team's offense */
  public double getOffense(String team) {
    assert offenses.containsKey(team);
    return offenses.get(team);
  }

  /** Sets the value of the parameter for the given team's offense. */
  public TeamModel setOffense(String team, double value) {
    assert offenses.containsKey(team);
    offenses.put(team, value);
    return this;
  }

  /** Returns the current value of the parameter for the given team's defense */
  public double getDefense(String team) {
    assert defenses.containsKey(team);
    return defenses.get(team);
  }

  /** Sets the value of the parameter for the given team's defense. */
  public TeamModel setDefense(String team, double value) {
    assert defenses.containsKey(team);
    defenses.put(team, value);
    return this;
  }

  /** Multiples every parameter in the model by {@code scale}. */
  public void scaleBy(double scale) {
    TeamModel result = new TeamModel();
    constant *= scale;
    for (String team : TEAMS) {
      offenses.put(team, scale * offenses.get(team));
      defenses.put(team, scale * defenses.get(team));
    }
  }

  /**
   * Adds to each parameter of this model scale times the value of the
   * corresponding parameter of the other model. (In pseudocode, it executes
   * {@code this += scale * other}.)
   */
  public TeamModel addScaledBy(double scale, TeamModel other) {
    constant += scale * other.constant;
    for (String team : TEAMS) {
      offenses.put(team,
          offenses.get(team) + scale * other.offenses.get(team));
      defenses.put(team,
          defenses.get(team) + scale * other.defenses.get(team));
    }
    return this;
  }

  /** Returns the L0 norm of the model thought of as a vector. */
  public double norm0() {
    double s = Math.abs(constant);
    for (String team : TEAMS) {
      s = Math.max(s, Math.abs(offenses.get(team)));
      s = Math.max(s, Math.abs(defenses.get(team)));
    } 
    return s;
  }

  /** Returns the L1 norm of the model thought of as a vector. */
  public double norm1() {
    double s = Math.abs(constant);
    for (String team : TEAMS) {
      s += Math.abs(offenses.get(team));
      s += Math.abs(defenses.get(team));
    } 
    return s;
  }

  /** Returns the L2 norm of the model thought of as a vector. */
  public double norm2() {
    double s = constant * constant;
    for (String team : TEAMS) {
      s += offenses.get(team) * offenses.get(team);
      s += defenses.get(team) * defenses.get(team);
    } 
    return Math.sqrt(s);
  }

  /** Returns the value of the loss function for the current model. */
  public double evalLoss(List<Drive> drives, double penalty) {
    double loss = 0;
    for (Drive drive : drives) {
      double actual = drive.expPointsAtEnd - drive.expPointsAtStart;
      double predicted = constant +
          offenses.get(drive.offense) - defenses.get(drive.defense);
      loss += (actual - predicted) * (actual - predicted);
    }
    return loss / drives.size() + penalty * norm1();
  }

  /**
   * Returns the derivative of the loss function, with <b>no penalty term</b>,
   * at the current model. (The penalty term is not diffentiable.) Since the
   * derivative has one coefficient for each parameter, we return it as a
   * {@code TeamModel}, even though it should not be interpreted as such.
   */
  public TeamModel evalLossDerivative(List<Drive> drives) {
    TeamModel derivative = new TeamModel();

    // Add the derivative of the main loss term.
    for (Drive drive : drives) {
      double actual = drive.expPointsAtEnd - drive.expPointsAtStart;
      double predicted = constant +
          offenses.get(drive.offense) - defenses.get(drive.defense);
      derivative.constant -= 2 * (actual - predicted);
      derivative.offenses.put(drive.offense,
          derivative.offenses.get(drive.offense) - 2 * (actual - predicted));
      derivative.defenses.put(drive.defense,
          derivative.defenses.get(drive.defense) + 2 * (actual - predicted));
    }

    return derivative;
  }

  /** Returns a count of the parameters with magnitude at least {@code tol}. */
  public int countNonZeroParameters(double tol) {
    int count = 0;
    if (Math.abs(constant) >= tol)
      count += 1;
    for (String team : TEAMS) {
      if (Math.abs(offenses.get(team)) >= tol)
        count += 1;
      if (Math.abs(defenses.get(team)) >= tol)
        count += 1;
    }
    return count;
  }

  /** Prints a description of this model to the given writer. */
  public void printTo(PrintStream out) throws IOException {
    out.printf("Constant: %5.2f\n\n", constant);
    out.println("     Off   Def");
    for (String team : TEAMS) {
      out.printf("%3s %5.2f %5.2f\n", team, offenses.get(team),
          defenses.get(team));
    }
  }
}
