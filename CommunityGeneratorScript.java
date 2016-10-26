package scripts.advancedwalking.collector;

import org.tribot.api.General;
import org.tribot.api.input.Mouse;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.Login;
import org.tribot.api2007.Projection;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.*;
import scripts.advancedwalking.generator.tiles.collector.collectors.RegionCollector;
import scripts.advancedwalking.generator.tiles.collector.TileCollector;
import scripts.entityselector.Entities;
import scripts.entityselector.finders.prefabs.ObjectEntity;
import scripts.lanapi.core.gui.GUI;
import scripts.lanapi.core.logging.LogProxy;
import scripts.lanapi.core.patterns.IStrategy;
import scripts.lanapi.game.concurrency.observers.inventory.InventoryListener;
import scripts.lanapi.game.helpers.ProjectionHelper;
import scripts.lanapi.game.painting.AbstractPaintInfo;
import scripts.lanapi.game.painting.PaintBuilder;
import scripts.lanapi.game.script.LANScript;
import scripts.lanapi.network.connectivity.DynamicSignatures;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;


@ScriptManifest(authors = {"Laniax"}, category = "AdvancedWalking", name = "CommunityGeneratorScript2")
public class CommunityGeneratorScript extends LANScript implements Painting, MouseActions, KeyActions, MousePainting, MouseSplinePainting, Arguments,
        EventBlockingOverride, Ending, Breaking, MessageListening07, InventoryListener, DynamicSignatures {

    LogProxy log = new LogProxy("CommunityGeneratorScript");

    private boolean scanningTiles = true;

    TileCollector collector;

    GUI gui;

    RSTile selected;

    /**
     * Return a JavaFX gui, it will automatically be shown and the script will wait until it closes.
     * Return null if you don't want a GUI.
     *
     * @return
     */
    @Override
    public GUI getGUI() {
        return gui = new GUI(this.getClass().getResource("gui.fxml"));
    }

    /**
     * Return a list of all the strategies this script should perform.
     *
     * @return
     */
    @Override
    public IStrategy[] getStrategies() {

        while (Login.getLoginState() != Login.STATE.INGAME)
            General.sleep(500);

        // Use a different tile collector in order to gather tiles in your own way
        // This opens up the possibility to read from the RS cache etc and all you have to do is replace this collector.
        collector = new RegionCollector();

        while (scanningTiles) {

            collector.collect();
        }

        log.info("Collected %d tiles!", collector.getTiles().size());


        return null;
    }

    /**
     * Return the notification icon for in the system tray bar.
     * Return null if you don't want an icon.
     *
     * @return
     */
    @Override
    public BufferedImage getNotificationIcon() {
        return null;
    }

    /**
     * Return this script's paint logic.
     * Return null if you don't want a paint.
     *
     * @return
     */
    @Override
    public AbstractPaintInfo getPaintInfo() {
        return new AbstractPaintInfo() {
            @Override
            public Color getPrimaryColor() {
                return new Color(102, 130, 142);
            }

            @Override
            public Color getSecondaryColor() {
                return new Color(102, 130, 142);
            }

            @Override
            public boolean isScriptPremium() {
                return false;
            }

            @Override
            public boolean showReportBugButton() {
                return false;
            }

            @Override
            public PaintBuilder paintTitle() {
                return new PaintBuilder().add().setText("AW Mesh Gen").end();
            }

            @Override
            public PaintBuilder getText(long runTime) {
                return new PaintBuilder().add().setText("Collecting tiles").end();
            }

            @Override
            public void customDrawBefore(Graphics2D g) {

                if (selected != null) {

                    if (selected.isOnScreen()) {

                        Polygon poly = Projection.getTileBoundsPoly(selected, 0);

                        if (poly != null) {

                            g.setColor(selectedTileColor);
                            g.fillPolygon(poly);

                        }

                    }
                }
            }
        };
    }

    Color blackTransparent = new Color(0, 0, 0, 100);
    Color selectedTileColor = new Color(255, 0, 1, 100);

    Rectangle stopButton = new Rectangle(380, 300, 130, 30);

//    @Override
//    public void onPaint(Graphics g) {
//
//        if (scanningTiles && collector != null) {
//            g.setColor(blackTransparent);
//            g.fillRect(stopButton.x, stopButton.y, stopButton.width, stopButton.height);
//            g.setColor(Color.BLACK);
//            g.drawRect(stopButton.x, stopButton.y, stopButton.width, stopButton.height);
//            g.setColor(Color.WHITE);
//            g.drawString("Generate", (stopButton.width / 3) + stopButton.x, (stopButton.height / 2) + stopButton.y + 5);
//            g.drawString("Number of tiles: " + collector.getTiles().size(), stopButton.x, stopButton.y - 10);
//
//            for (MeshTile t : collector.getTiles()) {
//
//
//                if (isDoorTile(t)) {
//
//
//
//                }
//
//
//            }
//        }
//
//    }

    private boolean isDoorTile(Positionable positionable) {

        if (positionable == null)
            return false;

        RSObject object = Entities.find(ObjectEntity::new)
                .actionsContains("Open", "Close")
                .tileEquals(positionable)
                .getFirstResult();

        return object != null;
    }

    private boolean isStairTile(Positionable positionable) {

        if (positionable == null)
            return false;

        RSObject object = Entities.find(ObjectEntity::new)
                .actionsContains("Climb")
                .tileEquals(positionable)
                .getFirstResult();

        return object != null;
    }


    @Override
    public void mouseClicked(Point point, int button, boolean isBot) {

        if (stopButton.contains(point.x, point.y)) {
            scanningTiles = false;
        }

    }

    public void keyReleased(int keycode, boolean is_bot) {

        if (is_bot)
            return;

        if (keycode == KeyEvent.VK_F1) {

            RSTile t = ProjectionHelper.getTileAtPoint(Mouse.getPos());

            if (t == null)
                return;

            selected = t;

            gui.<GUIController>getController().setTile(t);

        }

    }


    /**
     * The url of the server. In the following format:
     * http://yourdomain.com/scripts/yourscriptname/
     * <p>
     * Leave null if you dont want to use dynamic signatures.
     *
     * @return
     */
    @Override
    public String signatureServerUrl() {
        return null;
    }

    /**
     * Called every 5 minutes to send data to the server. Please send the values -from the script start- and NOT from since the last call.
     *
     * @return A hashmap with String that equals the a Type name on the server, and the integer value of the variable.
     */
    @Override
    public HashMap<String, Integer> signatureSendData() {
        return null;
    }


}
