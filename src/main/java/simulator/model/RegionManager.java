package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegionManager implements AnimalMapView {
    private int cols;
    private int rows;
    private int width;
    private int height;
    private Map<Animal, Region> animalRegion;
    private Region[][] regions;
    private int cellWidth;
    private int cellHeight;

    public RegionManager(int cols, int rows, int width, int height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.animalRegion = new HashMap<Animal, Region>();
        // Use Region[][] so we can store any Region subtype (e.g., DynamicSupplyRegion)
        this.regions = new Region[rows][cols];

        if (width % cols != 0 || height % rows != 0) {
            throw new IllegalArgumentException(
                    "Width and height must be divisible by columns and rows, respectively.");
        }

        // Cada celda tiene ancho = width/cols y alto = height/rows
        this.cellWidth = width / cols;
        this.cellHeight = height / rows;

        // Inicializo la matriz para que los valores de dentro de la matriz para que
        // estos no sean nulos.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.regions[i][j] = new DefaultRegion();
            }
        }
    }

    // Metodo que devuelve la estructura JSON.
    public JSONObject asJSON() {
        JSONObject result = new JSONObject();
        JSONArray arrayRegions = new JSONArray();

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                JSONObject datosRegions = new JSONObject();
                datosRegions.put("row", i);
                datosRegions.put("col", j);
                datosRegions.put("data", this.regions[i][j].asJSON());
                arrayRegions.put(datosRegions);
            }
        }

        result.put("regions", arrayRegions);
        return result;
    }

    @Override
    public List<Animal> getAnimalsInRange(Animal e, Predicate<Animal> filter) {
        List<Animal> visualRange = new ArrayList<>();
        // Para hacer el recorrido solo por las celdas que abarcan el campo de vision.
        // Para ello empiezo en la fila donde reto la vision a la pos x hasta la pos x
        // mas ese rango de vision, haciendo un cast para conseguir el entero.
        // Lo mismo para el eje Y.
        int colMin = (int) Math.floor((e.getPosition().getX() - e.getSightRange()) / cellWidth);
        int colMax = (int) Math.floor((e.getPosition().getX() + e.getSightRange()) / cellWidth);
        int filaMin = (int) Math.floor((e.getPosition().getY() - e.getSightRange()) / cellHeight);
        int filaMax = (int) Math.floor((e.getPosition().getY() + e.getSightRange()) / cellHeight);
        for (int i = filaMin; i <= filaMax; i++) { // aqui consulto las regiones del campo visual de Animal e
            for (int j = colMin; j <= colMax; j++) {
                if (this.intToMatrix(i, j)) {
                    for (Animal a : this.getRegion(i, j).getAnimals()) {
                        if (a != e && e.getPosition().distanceTo(a.getPosition()) <= e.getSightRange()
                                && filter.test(a)) {
                            visualRange.add(a);
                        }
                    }
                }

            }
        }
        return visualRange;
    }

    // Al ser un AnimalInfo, no se si se puede acceder como clave al mapa.
    @Override
    public double getfood(AnimalInfo a, double dt) {
        Region r = this.animalRegion.get(a);
        if (r == null) {
            return 0.0; // el animal aún no está registrado en ninguna región
        }
        return r.getfood(a, dt);
    }

    // Preguntar por esta funcion.
    public void setRegion(int row, int col, Region r) {
        Region oldRegion = this.regions[row][col];
        this.regions[row][col] = r;
        for (Animal a : oldRegion.getAnimals()) {
            r.addAnimal(a);
            this.getAnimalRegion().put(a, r);
        }
    }

    public void registerAnimal(Animal a) {
        // Aseguramos que el animal tiene posición inicial y destino coherentes.
        a.init(this);

        // En primer lugar tengo que transcribir las coordenadas del animal a (fila,
        // columna).
        int col = (int) Math.floor(a.getPosition().getX() / this.cellWidth);
        int fila = (int) Math.floor(a.getPosition().getY() / this.cellHeight); // Para quedarme solo con la parte
                                                                               // entera, hago un cast a int para
                                                                               // ignorar la parte decimal, que siempre
                                                                               // es 0 por que uso la formula de math de
                                                                               // redondear a lo bajo.
        if (this.intToMatrix(fila, col)) { // Compruebo si esta dentro del tablero.
            Region r = this.regions[fila][col];
            if (r != null) {
                r.addAnimal(a);
                this.animalRegion.put(a, r);
            }
        }
    }

    // Quita de la lista de animales de la region donde estaba el animal.
    public void unregisterAnimal(Animal a) {
        Region r = this.animalRegion.get(a); // Cojo las region del animal.

        if (r != null) { // Si no es que ese animal no existia en la lista.
            r.removeAnimal(a); // Quito al animal de la lista de animales de la region.
            this.animalRegion.remove(a); // Y borro la region que conecta a ese animal.
        }
    }

    public void updateanimalRegion(Animal a) {
        int col = (int) Math.floor(a.getPosition().getX() / this.cellWidth);
        int fila = (int) Math.floor(a.getPosition().getY() / this.cellHeight); // Para quedarme solo con la parte
                                                                               // entera, hago un cast a int para
                                                                               // ignorar la parte decimal.
        if (this.intToMatrix(fila, col)) {
            Region rNew = this.regions[fila][col]; // Region actual del animal a
            Region rOld = this.animalRegion.get(a); // Region que tenia antes el animal a.
            if (rNew != null && !rNew.equals(rOld)) {
                rNew.addAnimal(a);
            }
            if (rOld != null) {
                rOld.removeAnimal(a);
            }
            this.animalRegion.put(a, rNew);
        }
    }

    public void updateAllRegions(double dt) {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                this.regions[i][j].update(dt);
            }
        }
    }

    public boolean intToMatrix(int row, int col) {
        return row >= 0 && row < this.rows && col >= 0 && col < this.cols;
    }

    public Map<Animal, Region> getAnimalRegion() {
        return this.animalRegion;
    }

    public Region getRegion(int fila, int col) {
        return this.regions[fila][col];
    }

    public Region[][] getRegions() {
        return this.regions;
    }

    @Override
    public int getCols() {
        return this.cols;
    }

    @Override
    public int getRows() {
        return this.rows;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getRegionWidth() {
        return this.cellWidth;
    }

    @Override
    public int getRegionHeight() {
        return this.cellHeight;
    }
}
