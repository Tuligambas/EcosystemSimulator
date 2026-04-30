package simulator.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.State;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

  // Anchura/altura de la simulación -- se supone que siempre van a ser
  // iguales al tamaño del componente
  //
  // Width/height of the simulation -- they will always be equal to the size
  // of the component
  //
  private int width;
  private int height;

  // Número de filas/columnas de la simulación (regiones)
  //
  // Number of rows/cols of the simulation (regions)
  //
  private int rows;
  private int cols;

  // Anchura/altura de una región
  //
  // Width/height of a region
  //
  int rWidth;
  int rHeight;

  // Mostramos sólo animales con este estado. Los posibles valores de currState
  // son null, y los valores de Animal.State.values(). Si es null mostramos todo.
  //
  // We show the animals that have this state. The possible values of currentState
  // are: null and the values returned by Animal.State.values(). If it is null we
  // show all animals.
  //
  State currentState;

  // En estos atributos guardamos la lista de animales y el tiempo que hemos
  // recibido la última vez para dibujarlos.
  //
  // The value of these attributes are the list of animals and the time that we
  // have received in the notification (those will be shown).
  //
  volatile private Collection<AnimalInfo> objs;
  volatile private Double time;

  // Una clase auxiliar para almacenar información sobre una especie.
  //
  // An auxiliary class to store information about species.
  //
  private static class SpeciesInfo {
    private Integer count;
    private Color color;

    SpeciesInfo(Color color) {
      count = 0;
      this.color = color;
    }
  }

  // Un mapa para la información sobre las especies.
  //
  // A map with the information for each species.
  //
  Map<String, SpeciesInfo> kindsInfo = new HashMap<>();

  // El font que usamos para dibujar texto.
  //
  // The font to be used for drawing text.
  //
  private Font textFont = new Font("Arial", Font.BOLD, 12);

  // Indica si mostramos el texto la ayuda o no.
  //
  // Indicates if the 'help' information is visible or hidden.
  //
  private boolean showHelp;

  public MapViewer() {
    initGUI();
  }

  private void initGUI() {

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
          case 'h':
            showHelp = !showHelp;
            repaint();
            break;
          case 's':
            State[] states = State.values();

            if (currentState == null)
              currentState = states[0];

            else {
              // Buscar en qué posición está currentState
              int pos = 0;
              for (int i = 0; i < states.length; i++) {
                if (states[i] == currentState) {
                  pos = i;
                }
              }
              // Si es el último, volver a null. Si no, avanzar uno
              if (pos == states.length - 1) {
                currentState = null;
              } else {
                currentState = states[pos + 1];
              }
            }
            repaint();
            break;
          default:
        }
      }

    });

    addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(MouseEvent e) {
        // Esto es necesario para capturar las teclas cuando el ratón está sobre este
        // componente.
        //
        // This is needed to capture keystroke when the mouse is over this component.
        //
        requestFocus();
      }
    });

    // Por defecto mostramos todos los animales.
    //
    // By default, we show all animals.
    //
    currentState = null;

    // Por defecto mostramos el texto de ayuda.
    //
    // By default, the 'help' message is visible.
    //
    showHelp = true;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D gr = (Graphics2D) g;
    gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // Cambiar el font para dibujar texto.
    //
    // Change the font to be used when drawing text.
    //
    g.setFont(textFont);

    // Dibujar fondo blanco.
    //
    // Draw a white background.
    //
    gr.setBackground(Color.WHITE);
    gr.clearRect(0, 0, width, height);

    // Dibujar los animales, el tiempo, información sobre las especies, etc.
    //
    // Draw the animals, the time, species information. etc.
    //
    if (objs != null)
      drawObjects(gr, objs, time);

    // TODO Mostrar el texto de ayuda si showHelp es true. El texto a mostrar es el
    // siguiente (en 2 líneas):
    //
    // Show a 'help' text if showHelp is true. The text should be the following
    // in two separated lines:
    //
    // h: toggle help
    // s: show animals of a specific state
    if (showHelp) {
      g.setColor(Color.RED);
      g.drawString("h: toggle help", 10, 20);
      g.drawString("s: show animals of a specific state", 10, 40);
    }

  }

  private boolean visible(AnimalInfo a) {
    // TODO Devolver true si el animal es visible, es decir si currState es null o
    // su estado es igual a currState.
    //
    // return true of the animal is visible, i.e., currState is null or its
    // state is equal to currState.
    //

    return currentState == null || a.getState() == currentState;
  }

  private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {

    // TODO Dibujar el grid de regiones.
    //
    // Draw a grid of regions.
    //
    // Dibujar el grid de regiones
    g.setColor(Color.LIGHT_GRAY);

    // Líneas verticales
    for (int i = 0; i <= cols; i++) {
      g.drawLine(i * rWidth, 0, i * rWidth, height);
    }

    // Líneas horizontales
    for (int i = 0; i <= rows; i++) {
      g.drawLine(0, i * rHeight, width, i * rHeight);
    }

    // Dibujar los animales.
    //
    // Draw the animals
    //
    for (AnimalInfo a : animals) {

      // Si no es visible saltamos la iteración.
      //
      // If the animal is not visible, we skip to the next iteration.
      //
      if (!visible(a))
        continue;

      // La información sobre la especie de 'a'.
      //
      // Information of the species of 'a'
      //
      SpeciesInfo speciesInfo = kindsInfo.get(a.getGeneticCode());

      // TODO Si espInfo es null, añade una entrada correspondiente al mapa. Para el
      // color usa ViewUtils.getColor(a.getGeneticCode()).
      //
      // If espInfo is null, add a corresponding entry to the map. For the color
      // use ViewUtils.getColor(a.getGeneticCode()).
      if (speciesInfo == null) {
        speciesInfo = new SpeciesInfo(ViewUtils.get_color(a.getGeneticCode()));
        kindsInfo.put(a.getGeneticCode(), speciesInfo);
      }

      // TODO Incrementar el contador de la especie (es decir el contador dentro de
      // speciesInfo).
      //
      // Increment the counter of the species (i.e., the one inside speciesInfo).
      speciesInfo.count++;

      // TODO Dibujar el animal en la posición correspondiente, usando el color
      // speciesInfo.color. Su tamaño tiene que ser relativo a su edad, por ejemplo
      // edad/2+2. Se puede dibujar usando fillRoundRect, fillRect o fillOval.
      //
      // Draw the animal at the corresponding position, using the color
      // speciesInfo.color. Its size should be relative to the animal's age, e.g.,
      // age/2+2. For drawing you can use fillRoundRect, fillRect or fillOval.
      int size = (int) (a.getAge() / 2 + 2);
      int x = (int) a.getPosition().getX();
      int y = (int) a.getPosition().getY();
      g.setColor(speciesInfo.color);
      g.fillOval(x - size / 2, y - size / 2, size, size);
    }

    // TODO Dibujar la etiqueta del estado visible, usando currState.toString(), si
    // no
    // es null.
    //
    // Draw the tag of the visible state, using currState.toString(), if it is not
    // null.
    // Empezamos desde abajo
    int yPos = height - 20;

    // Estado visible (más abajo de todo, solo si no es null)
    if (currentState != null) {
      g.setColor(Color.RED);
      drawStringWithRect(g, 5, yPos, "State: " + currentState.toString());
      yPos -= 20;
    }

    // Tiempo
    g.setColor(Color.BLACK);
    drawStringWithRect(g, 5, yPos, "Time: " + String.format("%.3f", time));
    yPos -= 20;

    // Información de especies (encima del tiempo)
    for (Entry<String, SpeciesInfo> e : kindsInfo.entrySet()) {
      g.setColor(e.getValue().color);
      drawStringWithRect(g, 5, yPos, e.getKey() + ": " + e.getValue().count);
      yPos -= 20;
      e.getValue().count = 0;
    }

  }

  // Un método que dibujar un texto con un rectángulo.
  //
  // A method for drawing a text with a rectangular border.
  //
  void drawStringWithRect(Graphics2D g, int x, int y, String s) {
    Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
    g.drawString(s, x, y);
    g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
  }

  @Override
  public void update(List<AnimalInfo> objs, Double time) {
    // TODO Almacenar objs y time en los atributos correspondientes y llamar a
    // repaint() para redibujar el componente.
    //
    // Store objs and time in the corresponding fields, and call repaint() to
    // redraw the component.
    this.objs = objs;
    this.time = time;
    repaint();

  }

  @Override
  public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
    // TODO Actualizar los atributos width, height, cols, rows, etc.
    //
    // Update the fields width, height, cols, rows, etc.

    // Esto cambia el tamaño del componente, y así cambia el tamaño de la ventana
    // porque en MapWindow llamamos a pack() después de llamar a reset.
    //
    // This line changes the size of the component, and thus the size of the window
    // because MapWindow calls pack() after calling reset().
    //
    width = map.getWidth();
    height = map.getHeight();
    cols = map.getCols();
    rows = map.getRows();
    rWidth = map.getRegionWidth();
    rHeight = map.getRegionHeight();
    setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));

    // Dibuja el estado.
    //
    // Draw the state.
    //
    update(animals, time);
  }

}