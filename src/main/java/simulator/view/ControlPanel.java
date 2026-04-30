package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;

class ControlPanel extends JPanel {

  private Controller ctrl;
  private ChangeRegionsDialog changeRegionsDialog;

  private JToolBar toolBar;
  private JFileChooser fc;
  private boolean stopped = true; // utilizado en los botones de run/stop
  private JButton quitButton;

  private JButton loadButton;
  private JButton viewerButton;
  private JButton runButton;
  private JButton stopButton;
  private JButton changeRegionsButton;
  private JSpinner stepsSpinner;
  private JSpinner dtField;

  ControlPanel(Controller ctrl) {
    this.ctrl = ctrl;
    initGUI();
  }

  private void initGUI() {
    setLayout(new BorderLayout());
    toolBar = new JToolBar();
    toolBar.setFloatable(false);
    add(toolBar, BorderLayout.PAGE_START);

    this.fc = new JFileChooser();
    this.fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/src/main/resources/examples"));

    actionLoadButton();
    toolBar.addSeparator();

    actionViewerButton();
    toolBar.addSeparator();

    actionChangeRegionsButton();
    toolBar.addSeparator();

    actionRunButton();
    actionStopButton();
    toolBar.addSeparator();

    actionStepsSpinner();
    toolBar.addSeparator();

    actionDtField();

    // Quit Button
    this.toolBar.add(Box.createGlue()); // this aligns the button to the right
    this.toolBar.addSeparator();
    this.quitButton = new JButton();
    this.quitButton.setToolTipText("Exit");
    URL exitIcon = getClass().getClassLoader().getResource("icons/exit.png");
    this.quitButton.setIcon(new ImageIcon(exitIcon));
    this.quitButton.addActionListener((e) -> ViewUtils.quit(this));
    this.toolBar.add(quitButton);

    this.changeRegionsDialog = new ChangeRegionsDialog(ctrl);
  }

  // para cargar ficheros
  private void actionLoadButton() {
    this.loadButton = createButton(
        "icons/open.png",
        "Load an input file to the simulator",
        (e) -> {
          int result = fc.showOpenDialog(ViewUtils.getWindow(this)); // abrir el selector de ficheros
          if (result == JFileChooser.APPROVE_OPTION) {
            try {
              File f = fc.getSelectedFile();
              JSONObject data = new JSONObject(new JSONTokener(new FileInputStream(f))); // cárgalo como JSONObject
              ctrl.reset( // resetea el simulador utilizando this.ctrl.reset(...)
                  data.getInt("cols"),
                  data.getInt("rows"),
                  data.getInt("width"),
                  data.getInt("height"));
              ctrl.loadData(data); // carga el json usando this.ctrl.loadData(...)
            } catch (Exception ex) {
              ViewUtils.showErrorMsg(ViewUtils.getWindow(this), "Error loading file: " + ex.getMessage());
            }
          }
        });
    toolBar.add(loadButton);
  }

  // mapa de la simulacion
  private void actionViewerButton() {
    this.viewerButton = createButton(
        "icons/viewer.png",
        "Map viewer",
        (e) -> new MapWindow(ViewUtils.getWindow(this), ctrl));
    toolBar.add(viewerButton);
  }

  // Spinner de pasos, codigo reciclado
  private void actionStepsSpinner() {
    this.stepsSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 1000000, 100));
    this.stepsSpinner.setToolTipText("Simulation setps to run: 1-100000");
    this.stepsSpinner.setPreferredSize(new Dimension(80, 30));
    toolBar.add(new JLabel(" Steps: "));
    toolBar.add(stepsSpinner);
  }

  private void actionDtField() {
    this.dtField = new JSpinner(new SpinnerNumberModel(0.03, 0.001, 10.0, 0.01));
    this.dtField.setToolTipText("Real time (seconds) corresponding to a step");
    this.dtField.setPreferredSize(new Dimension(80, 30));
    this.dtField.setMaximumSize(this.dtField.getPreferredSize());
    toolBar.add(new JLabel(" Delta-time: "));
    toolBar.add(dtField);
  }

  // Run button
  private void actionRunButton() {
    this.runButton = createButton(
        "icons/run.png",
        "Run the simulator",
        (e) -> {
          double dt = ((Number) dtField.getValue()).doubleValue();
          enableControls(false);
          stopped = false;
          runSim((Integer) stepsSpinner.getValue(), dt);
        });
    toolBar.add(runButton);
  }

  // btn stop
  private void actionStopButton() {
    this.stopButton = createButton(
        "icons/stop.png",
        "Stops the simulator",
        (e) -> stopped = true);
    toolBar.add(stopButton);
  }

  // btn para cambiar regiones
  private void actionChangeRegionsButton() {
    this.changeRegionsButton = createButton(
        "icons/regions.png",
        "Change regions",
        (e) -> changeRegionsDialog.open(ViewUtils.getWindow(this)));
    toolBar.add(changeRegionsButton);
  }

  private void runSim(int n, double dt) {
    if (n > 0 && !this.stopped) {
      try {
        this.ctrl.advance(dt);
        SwingUtilities.invokeLater(() -> runSim(n - 1, dt));
      } catch (Exception e) {
        ViewUtils.showErrorMsg(ViewUtils.getWindow(this), "Error during simulation: " + e.getMessage());
        enableControls(true);
        this.stopped = true;
      }
    } else {
      enableControls(true);
      this.stopped = true;
    }
  }

  // para deshabilitar o habilitar botones
  private void enableControls(boolean enabled) {
    loadButton.setEnabled(enabled);
    viewerButton.setEnabled(enabled);
    runButton.setEnabled(enabled);
    changeRegionsButton.setEnabled(enabled);
    stepsSpinner.setEnabled(enabled);
    dtField.setEnabled(enabled);
  }

  // Crea un boton
  private JButton createButton(String iconPath, String tooltip, ActionListener action) {
    JButton button = new JButton();
    button.setToolTipText(tooltip);
    URL iconUrl = getClass().getClassLoader().getResource(iconPath);
    if (iconUrl != null)
      button.setIcon(new ImageIcon(iconUrl));
    else
      button.setText(tooltip);
    if (action != null)
      button.addActionListener(action);

    return button;
  }
}
