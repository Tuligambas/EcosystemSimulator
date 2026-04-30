package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import simulator.control.Controller;

public class MainWindow extends JFrame {

  private Controller ctrl;

  public MainWindow(Controller ctrl) {
    super("[ECOSYSTEM SIMULATOR]");
    this.ctrl = ctrl;
    initGUI();
  }

  private void initGUI() {
    JPanel mainPanel = new JPanel(new BorderLayout());
    setContentPane(mainPanel);

    mainPanel.add(new ControlPanel(ctrl), BorderLayout.PAGE_START);
    mainPanel.add(new StatusBar(ctrl), BorderLayout.PAGE_END);

    // Definición del panel de tablas (usa un BoxLayout vertical)
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    mainPanel.add(contentPanel, BorderLayout.CENTER);

    // tables
    InfoTable speciesView = new InfoTable("Species", new SpeciesTableModel(ctrl));
    speciesView.setPreferredSize(new Dimension(500, 250));
    contentPanel.add(speciesView);

    InfoTable regionsView = new InfoTable("Regions", new RegionsTableModel(ctrl));
    regionsView.setPreferredSize(new Dimension(500, 250));
    contentPanel.add(regionsView);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        ViewUtils.quit(MainWindow.this); // abre el panel de confirmación
      }
    });

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    pack();
    setVisible(true);
  }
}