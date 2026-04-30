package simulator.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class MapWindow extends JFrame implements EcoSysObserver {

  private Controller ctrl;
  private AbstractMapViewer viewer;
  private Frame parent;

  MapWindow(Frame parent, Controller ctrl) {
    super("[MAP VIEWER]");
    this.ctrl = ctrl;
    this.parent = parent;
    intiGUI();
    ctrl.addObserver(this);
  }

  private void intiGUI() {
    JPanel mainPanel = new JPanel(new BorderLayout());
    setContentPane(mainPanel);

    viewer = new MapViewer();
    mainPanel.add(viewer, BorderLayout.CENTER);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        ctrl.removeObserver(MapWindow.this);
      }
    });

    pack();
    if (this.parent != null)
      setLocation(
          this.parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
          this.parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
    setResizable(false);
    setVisible(true);
  }

  @Override
  public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
    SwingUtilities.invokeLater(() -> {
      viewer.reset(time, map, animals);
      pack();
    });
  }

  @Override
  public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
    SwingUtilities.invokeLater(() -> {
      viewer.reset(time, map, animals);
      pack();
    });
  }

  @Override
  public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
  }

  @Override
  public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
  }

  @Override
  public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
    SwingUtilities.invokeLater(() -> viewer.update(animals, time));
  }
}
