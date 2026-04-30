package simulator.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {

  private List<MapInfo.RegionData> regionsList;

  RegionsTableModel(Controller ctrl) {
    regionsList = new ArrayList<>();
    ctrl.addObserver(this);
  }

  @Override
  public int getRowCount() {
    return regionsList.size();
  }

  @Override
  public int getColumnCount() {
    return 3 + Diet.values().length;
  }

  @Override
  public String getColumnName(int col) {
    if (col == 0)
      return "Row";
    if (col == 1)
      return "Col";
    if (col == 2)
      return "Desc";
    return Diet.values()[col - 3].toString();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    MapInfo.RegionData region = regionsList.get(rowIndex);
    if (columnIndex == 0)
      return region.row();
    if (columnIndex == 1)
      return region.col();
    if (columnIndex == 2)
      return region.r().toString();
    return countByDiet(region.r(), Diet.values()[columnIndex - 3]);
  }

  private int countByDiet(RegionInfo region, Diet diet) {
    int count = 0;
    for (AnimalInfo a : region.getAnimalsInfo()) {
      if (a.getDiet() == diet) {
        count++;
      }
    }
    return count;
  }

  private void setRegionsList(MapInfo map) {
    regionsList = new ArrayList<>();
    for (MapInfo.RegionData i : map) {
      regionsList.add(i);
    }
    fireTableStructureChanged();
  }

  @Override
  public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
    setRegionsList(map);
  }

  @Override
  public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
    setRegionsList(map);
  }

  @Override
  public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
    setRegionsList(map);
  }

  @Override
  public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
    setRegionsList(map);
  }

  @Override
  public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
    setRegionsList(map);
  }
}
