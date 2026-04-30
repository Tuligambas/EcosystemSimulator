package simulator.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {

  private List<AnimalInfo> animalsList;
  private List<String> speciesList;
  private Map<String, Map<State, Integer>> counters; // por cada especie, contiene un map de {estado, contador}

  SpeciesTableModel(Controller ctrl) {
    animalsList = new ArrayList<>();
    speciesList = new ArrayList<>();
    counters = new LinkedHashMap<>();
    ctrl.addObserver(this);
  }

  @Override
  public int getRowCount() {
    return speciesList.size();
  }

  @Override
  public int getColumnCount() {
    return 1 + State.values().length;
  }

  @Override
  public String getColumnName(int col) {
    if (col == 0)
      return "Species";
    return State.values()[col - 1].toString();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    String species = speciesList.get(rowIndex);
    Map<State, Integer> stateCounter = counters.get(species);
    if (columnIndex == 0)
      return species;
    return stateCounter.getOrDefault(State.values()[columnIndex - 1], 0);
  }

  private void setAnimalsList(List<AnimalInfo> animals) {
    animalsList = animals;
    counters = new LinkedHashMap<>();
    for (AnimalInfo a : animalsList) {
      String code = a.getGeneticCode();
      if (!counters.containsKey(code)) {
        counters.put(code, new LinkedHashMap<>());
      }
      Map<State, Integer> stateCounts = counters.get(code);
      if (!stateCounts.containsKey(a.getState())) {
        stateCounts.put(a.getState(), 0);
      }
      stateCounts.put(a.getState(), stateCounts.get(a.getState()) + 1);
    }
    speciesList = new ArrayList<>(counters.keySet());
    fireTableStructureChanged();
  }

  @Override
  public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
    setAnimalsList(animals);
  }

  @Override
  public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
    setAnimalsList(animals);
  }

  @Override
  public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
    setAnimalsList(animals);
  }

  @Override
  public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
    setAnimalsList(animals);
  }

  @Override
  public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
    // no afecta a la lista de animales
  }
}
