package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class StatusBar extends JPanel implements EcoSysObserver {

  private JLabel timeLabel;
  private JLabel animalsLabel;
  private JLabel dimensionsLabel;

  StatusBar(Controller ctrl) {
    initGUI();
    ctrl.addObserver(this);
  }

  private void initGUI() {
    this.setLayout(new FlowLayout(FlowLayout.LEFT));
    this.setBorder(BorderFactory.createBevelBorder(1));

    // Tiempo
    this.add(new JLabel("Time: "));
    this.timeLabel = new JLabel("0.0");
    this.add(timeLabel);

    addSeparator();

    // Número de animales
    this.add(new JLabel("Total Animals: "));
    this.animalsLabel = new JLabel("0");
    this.add(animalsLabel);

    addSeparator();

    // Dimensiones del mapa
    this.add(new JLabel("Dimension: "));
    this.dimensionsLabel = new JLabel("-");
    this.add(dimensionsLabel);
  }

  // Funcion proporcionada en la plantilla
  private void addSeparator() {
    JSeparator s = new JSeparator(JSeparator.VERTICAL);
    s.setPreferredSize(new Dimension(10, 20));
    this.add(s);
  }

  // Actualiza las etiquetas con los datos más recientes
  private void update(double time, MapInfo map, List<AnimalInfo> animals) {
    timeLabel.setText(String.format("%.3f", time));
    animalsLabel.setText(String.valueOf(animals.size()));
    if (map != null) {
      dimensionsLabel.setText(
          map.getWidth() + "x" + map.getHeight() + " " + map.getCols() + "x" + map.getRows());
    }
  }

  @Override
  public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
    update(time, map, animals);
  }

  @Override
  public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
    update(time, map, animals);
  }

  @Override
  public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
    update(time, map, animals);
  }

  @Override
  public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
    // Las regiones no cambian tiempo ni animales; no hay nada que actualizar aquí
  }

  @Override
  public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
    update(time, map, animals);
  }
}
