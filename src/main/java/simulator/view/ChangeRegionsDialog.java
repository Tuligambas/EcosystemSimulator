package simulator.view;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class ChangeRegionsDialog extends JDialog implements EcoSysObserver {

  private DefaultComboBoxModel<String> regionsModel;
  private DefaultComboBoxModel<String> fromRowModel;
  private DefaultComboBoxModel<String> toRowModel;
  private DefaultComboBoxModel<String> fromColModel;
  private DefaultComboBoxModel<String> toColModel;

  private DefaultTableModel dataTableModel;
  private Controller ctrl;
  private List<JSONObject> regionsInfo;

  private String[] headers = { "Key", "Value", "Description" };

  private int status;

  ChangeRegionsDialog(Controller ctrl) {
    super((Frame) null, true);
    this.ctrl = ctrl;
    initGUI();
    ctrl.addObserver(this);
  }

  private void initGUI() {
    // panel principal en el que meto otros paneles del dialogo
    setTitle("Change Regions");
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    setContentPane(mainPanel);

    // creo los paneles necesarios para el dialogo
    JPanel helpTextPanel = new JPanel();
    helpTextPanel.setLayout(new BoxLayout(helpTextPanel, BoxLayout.Y_AXIS));
    mainPanel.add(helpTextPanel);

    JPanel tablePanel = new JPanel();
    tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
    mainPanel.add(tablePanel);

    JPanel comboBoxPanel = new JPanel();
    comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.X_AXIS));
    mainPanel.add(comboBoxPanel);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    mainPanel.add(buttonPanel);

    // crear el texto de ayuda que aparece en la parte superior del diálogo
    JLabel helpText = new JLabel(
        "<html>Select a region type, the rows/cols interval, and provide values for the parametes in the <b>Value column</b> (default values are used for parametes with no value).</html>");
    helpText.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
    helpTextPanel.add(helpText);

    // this.regionsInfo se usará para establecer la información en la tabla
    this.regionsInfo = Main.getRegionsFactory().getInfo();

    // this.dataTableModel es un modelo de tabla que incluye todos los parámetros de
    // la region
    this.dataTableModel = new DefaultTableModel() {
      @Override
      public boolean isCellEditable(int row, int column) {
        // hace editable solo la columna 1
        return column == 1;
      }
    };
    this.dataTableModel.setColumnIdentifiers(this.headers);

    // creo un JTable que use dataTableModel, y lo añado al diálogo
    JTable table = new JTable(dataTableModel);
    tablePanel.add(new JScrollPane(table));

    // this.regionsModel es un modelo de combobox que incluye los tipos de regiones
    this.regionsModel = new DefaultComboBoxModel<>();

    // TODO añadir la descripción de todas las regiones a regionsModel. Para eso
    // usa la clave “desc” o “type” de los JSONObject en regionsInfo,
    // ya que estos nos dan información sobre lo que puede crear la factoría.
    for (JSONObject region : regionsInfo) {
      regionsModel.addElement(region.getString("desc"));
    }

    // creo un combobox que use regionsModel y lo añado al diálogo.
    JComboBox<String> regionsComboBox = new JComboBox<>(regionsModel);
    comboBoxPanel.add(new JLabel("Region type:"));
    comboBoxPanel.add(regionsComboBox);

    // cuando el usuario selecciona una región, actualizo la tabla con sus claves y
    // descripciones
    regionsComboBox.addActionListener(e -> {
      int i = regionsComboBox.getSelectedIndex();
      if (i >= 0) {
        JSONObject info = regionsInfo.get(i);
        JSONObject data = info.getJSONObject("data");
        dataTableModel.setRowCount(0);
        for (String key : data.keySet()) {
          dataTableModel.addRow(new Object[] { key, "", data.get(key) });// col 1 va la clave, 2 vacia, 3 el valor
        }
      }
    });

    // creo 4 modelos de combobox para this.fromRowModel, this.toRowModel,
    // this.fromColModel y this.toColModel.
    this.fromRowModel = new DefaultComboBoxModel<>();
    this.toRowModel = new DefaultComboBoxModel<>();
    this.fromColModel = new DefaultComboBoxModel<>();
    this.toColModel = new DefaultComboBoxModel<>();

    // creo 4 combobox que usen estos modelos y añadirlos al diálogo.
    JComboBox<String> fromRowCombo = new JComboBox<>(fromRowModel);
    JComboBox<String> toRowCombo = new JComboBox<>(toRowModel);
    JComboBox<String> fromColCombo = new JComboBox<>(fromColModel);
    JComboBox<String> toColCombo = new JComboBox<>(toColModel);
    comboBoxPanel.add(new JLabel("Row from/to:"));
    comboBoxPanel.add(fromRowCombo);
    comboBoxPanel.add(toRowCombo);
    comboBoxPanel.add(new JLabel("Column from/to:"));
    comboBoxPanel.add(fromColCombo);
    comboBoxPanel.add(toColCombo);

    // crear los botones OK y Cancel y añadirlos al diálogo.
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");

    okButton.addActionListener(e -> {
      try {
        // Construyo region_data con las filas no vacías de la tabla
        JSONObject regionData = new JSONObject();
        for (int i = 0; i < dataTableModel.getRowCount(); i++) {
          String key = dataTableModel.getValueAt(i, 0).toString();
          Object value = dataTableModel.getValueAt(i, 1);
          if (value != null && !value.toString().isEmpty()) { // me aseguro que no pueda estar vacio
            regionData.put(key, value.toString());
          }
        }

        // Saco el tipo de región seleccionada
        int selectedIndex = regionsComboBox.getSelectedIndex();
        String regionType = regionsInfo.get(selectedIndex).getString("type");

        // Saco las coordenadas
        int row_from = Integer.parseInt(fromRowCombo.getSelectedItem().toString());
        int row_to = Integer.parseInt(toRowCombo.getSelectedItem().toString());
        int col_from = Integer.parseInt(fromColCombo.getSelectedItem().toString());
        int col_to = Integer.parseInt(toColCombo.getSelectedItem().toString());

        // Monto el JSON final
        JSONObject spec = new JSONObject();
        spec.put("type", regionType);
        spec.put("data", regionData);

        JSONObject regionEntry = new JSONObject();
        regionEntry.put("row", new JSONArray().put(row_from).put(row_to));
        regionEntry.put("col", new JSONArray().put(col_from).put(col_to));
        regionEntry.put("spec", spec);

        JSONObject json = new JSONObject();
        json.put("regions", new JSONArray().put(regionEntry));

        // LLmada al al controlador
        ctrl.setRegions(json);
        status = 1;
        setVisible(false);
      } catch (Exception ex) {
        ViewUtils.showErrorMsg(ex.getMessage());
      }
    });

    cancelButton.addActionListener(e -> {
      status = 0;
      setVisible(false);
    });

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    setPreferredSize(new Dimension(750, 400));
    pack();
    setResizable(false);
    setVisible(false);
  }

  public void open(Frame parent) {
    setLocation(
        parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
        parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
    pack();
    setVisible(true);
  }

  private void updateCombos(MapInfo map) {
    fromRowModel.removeAllElements();
    toRowModel.removeAllElements();
    for (int i = 0; i < map.getRows(); i++) {
      fromRowModel.addElement(String.valueOf(i));
      toRowModel.addElement(String.valueOf(i));
    }
    fromColModel.removeAllElements();
    toColModel.removeAllElements();
    for (int i = 0; i < map.getCols(); i++) {
      fromColModel.addElement(String.valueOf(i));
      toColModel.addElement(String.valueOf(i));
    }
  }

  @Override
  public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
    updateCombos(map);
  }

  @Override
  public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
    updateCombos(map);
  }

  @Override
  public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
  }

  @Override
  public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
  }

  @Override
  public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
  }
}
