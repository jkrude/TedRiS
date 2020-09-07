import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SortingHatRiddle {


  public static void main(String[] args) {

    Deque<Founder> founders = new ArrayDeque<>(List.of(
        new Founder(Name.DEEPMIRE, HatColor.BLUE, HatSymbol.STARS),
        new Founder(Name.FUNFLAME, HatColor.RED, HatSymbol.SWIRLS),
        new Founder(Name.HYPNOTUM, HatColor.RED, HatSymbol.STARS),
        new Founder(Name.IMAGINEZ, HatColor.YELLOW, HatSymbol.MOONS),
        new Founder(Name.MIRACULO, HatColor.RED, HatSymbol.MOONS),
        new Founder(Name.RIMLEBY, HatColor.BLUE, HatSymbol.MOONS),
        new Founder(Name.SEPTIMUS, HatColor.YELLOW, HatSymbol.STARS),
        new Founder(Name.TREMENDA, HatColor.BLUE, HatSymbol.SWIRLS))
    );

    List<Constraint<Founder, House>> constraints = List.of(
        // There cant be more than to founder to one house.
        (currX, selectedY, mappings) -> Collections.frequency(mappings.values(), selectedY) <= 1,
        // A founder can only be mapped to one house (not really needed)
        (currX, selectedY, mappings) -> !mappings.containsKey(currX),
        // Funflame and Imaginzed share two options
        new TwoShareTwo(Name.FUNFLAME, Name.IMAGINEZ, House.GIANTEYE, House.LONGMOUS),
        new TwoShareTwo(Name.MIRACULO, Name.RIMLEBY, House.LONGMOUS, House.MERAMAID),
        // Same color/symbol are not possible
        (currX, selectedY, mappings) -> mappings.entrySet().stream()
            .filter(entry -> entry.getValue() == selectedY)
            .allMatch(entry -> (currX.hatSymbol != entry.getKey().hatSymbol)
                && (currX.hatColor != entry.getKey().hatColor)),
        // Septimus has one choice less
        (currX, selectedY, mappings) -> currX.name != Name.SEPTIMUS || selectedY != House.VIDOPNIR);

    Solver<Founder, House> solver = new Solver<>(founders, Arrays.asList(House.values()),
        constraints);
    Optional<Map<Founder, House>> optSolution = solver.findOne();
    String output;
    output = optSolution.isPresent() ? optSolution.toString() : "No solution found";
    System.out.println(output);
  }

  enum Name {
    DEEPMIRE,
    FUNFLAME,
    HYPNOTUM,
    IMAGINEZ,
    MIRACULO,
    RIMLEBY,
    SEPTIMUS,
    TREMENDA
  }

  enum House {
    GIANTEYE,
    MERAMAID,
    LONGMOUS,
    VIDOPNIR
  }

  enum HatColor {
    RED,
    BLUE,
    YELLOW
  }

  enum HatSymbol {
    STARS,
    SWIRLS,
    MOONS
  }

  public static class Founder {

    public Name name;
    public HatColor hatColor;
    public HatSymbol hatSymbol;

    public Founder(Name name, HatColor hatColor, HatSymbol hatSymbol) {
      this.name = name;
      this.hatColor = hatColor;
      this.hatSymbol = hatSymbol;

    }

    @Override
    public String toString() {
      return name.toString();
    }
  }

  public static class TwoShareTwo implements Constraint<Founder, House> {

    Name founderA, founderB;
    House opt1, opt2;

    public TwoShareTwo(Name founderA, Name founderB, House opt1, House opt2) {
      this.founderA = founderA;
      this.founderB = founderB;
      this.opt1 = opt1;
      this.opt2 = opt2;
    }

    @Override
    public boolean test(Founder currX, House selectedY, Map<Founder, House> mappings) {
      if (currX.name != founderA && currX.name != founderB) {
        return true; // Constraint does not matter.
      } else {
        if (selectedY != opt1 && selectedY != opt2) {
          return false; // It has to be one of the options.
        } else {
          // It is one of the founders and one of the options is selected.

          if (currX.name == founderA) {
            // Possible/True if the other one was not already mapped to the house.
            return isNotMappedTo(founderB, selectedY, mappings);
          } else {
            return isNotMappedTo(founderA, selectedY, mappings);
          }
        }
      }
    }

    private boolean isNotMappedTo(Name founderName, House h, Map<Founder, House> mapping) {
      return mapping.entrySet().stream()
          .filter(entry -> entry.getKey().name == founderName)
          .allMatch(entry -> entry.getValue() != h);
    }
  }
}
