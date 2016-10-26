package scripts.advancedwalking.collector;

import com.allatori.annotations.DoNotRename;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.entityselector.Entities;
import scripts.entityselector.finders.prefabs.ObjectEntity;
import scripts.lanapi.core.gui.AbstractGUIController;
import scripts.lanapi.game.helpers.ObjectsHelper;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Laniax
 */
@DoNotRename
public class GUIController extends AbstractGUIController {

    private final Path profileDirectory = Paths.get(System.getenv("APPDATA") + "\\.tribot\\AdvancedWalking\\");

    private RSTile tile;
    private RSObject object;
    private RSArea selectedArea;

    @FXML
    @DoNotRename
    private Text selectedTile;

    @FXML
    @DoNotRename
    private ComboBox<ObjectTypes> selectedObjectType;

    @FXML
    @DoNotRename
    private Text selectedObjectName;

    @FXML
    @DoNotRename
    private Button doneDrawingArea;

    @FXML
    @DoNotRename
    private Button deleteSelectedArea;

    @FXML
    @DoNotRename
    private ComboBox<AreaTypes> selectedAreaType;

    @FXML
    @DoNotRename
    private Button sendData;

    @FXML
    @DoNotRename
    private Button getData;

    @FXML
    @DoNotRename
    private ProgressIndicator receiveProgress;

    @FXML
    @DoNotRename
    private ProgressIndicator sendProgress;

    @Override
    public boolean getEnableNotifications() {
        return false;
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        selectedObjectType.getItems().addAll(ObjectTypes.values());
        selectedAreaType.getItems().addAll(AreaTypes.values());

    }

    public RSTile getTile() {
        return tile;
    }

    public void setTile(RSTile tile) {

        if (tile == null)
            return;

        this.tile = tile;

        selectedTile.setText(String.format("new RSTile(%d, %d, %d);", tile.getX(), tile.getY(), tile.getPlane()));

        object = Entities.find(ObjectEntity::new)
                .tileEquals(tile)
                .nameNotEquals("null")
                .getFirstResult();

        setObject(object);
    }

    public void setObject(RSObject object) {

        if (object != null) {
            selectedObjectName.setText(ObjectsHelper.getName(object));
        } else {
            selectedObjectName.setText("...");
        }

    }
}
