package LokEngineSceneEditor.GUI;

import LokEngine.Components.AnimationComponent;
import LokEngine.Components.Component;
import LokEngine.Components.RigidbodyComponent;
import LokEngine.Components.SpriteComponent;
import LokEngine.GUI.AdditionalObjects.GUIButtonScript;
import LokEngine.GUI.AdditionalObjects.GUIObjectProperties;
import LokEngine.GUI.Canvases.GUICanvas;
import LokEngine.GUI.GUIObjects.*;
import LokEngine.Render.Frame.PartsBuilder;
import LokEngine.SceneEnvironment.SceneObject;
import LokEngine.Tools.DefaultFields;
import LokEngine.Tools.RuntimeFields;
import LokEngine.Tools.Utilities.Vector2i;
import LokEngineSceneEditor.GUI.ComponentsWindow.AvailableComponentsListWindow;
import LokEngineSceneEditor.Render.FrameParts.ShapesRenderFramePart;
import LokEngineSceneEditor.SceneInteraction.CameraMovement;
import LokEngineSceneEditor.SceneInteraction.ObjectHighlight;

import static LokEngineSceneEditor.GUI.MainStyle.*;
import static LokEngineSceneEditor.GUI.SceneObjectsListPanel.freeTextDrawer;
import static LokEngineSceneEditor.LokEngineSceneEditor.availableComponentsListWindow;

public class SceneObjectComponentsPanel extends GUIObject {

    GUIFreeTextDrawer freeTextDrawer;
    Vector2i textGap;
    GUICanvas canvas;
    boolean lastPress = false;

    public static Component selectedComponent;

    public SceneObjectComponentsPanel(Vector2i position, Vector2i size) {
        super(position, size);
        freeTextDrawer = new GUIFreeTextDrawer("",0,12,true);
        textGap = new Vector2i(0,15);
        canvas = new GUICanvas(position,size);

        GUIButton buttonAdd = new GUIButton(new Vector2i(size.x - 20,0),new Vector2i(20,20),panelsColor,panelsColor,
                new GUIText(new Vector2i(),"+",textColor,0,14),
                new GUIPanel(new Vector2i(),new Vector2i()));

        buttonAdd.setPressScript(guiButton -> {
            availableComponentsListWindow.hidden = false;
            availableComponentsListWindow.setGUIButtonScript(guiButton1 -> {

                SceneObject object = ObjectHighlight.getHighlightedObject();
                if (guiButton1.text.getText().equals("Sprite Component")){
                    if (object != null){
                        object.components.add(new SpriteComponent("<Sprite Path>"));
                    }
                }else if (guiButton1.text.getText().equals("Animation Component")){
                    if (object != null){
                        object.components.add(new AnimationComponent());
                    }
                }

            });
        });

        canvas.addObject(buttonAdd);
        canvas.addObject(freeTextDrawer);

    }

    @Override
    public void update(PartsBuilder partsBuilder, GUIObjectProperties parentProperties){
        super.update(partsBuilder,parentProperties);
        SceneObject sceneObject = ObjectHighlight.getHighlightedObject();

        if (sceneObject != null){
            int componentsSize = sceneObject.components.getSize();

            boolean thisIsThatObject = false;
            boolean mousePressed = parentProperties.window.getMouse().getPressedStatus();
            int toRemove = -1;

            for (int i = 0; i < componentsSize; i++){
                Component component = sceneObject.components.get(i);

                Vector2i textPos = new Vector2i(getPosition().x - 10,textGap.y * i + getPosition().y + 30);
                boolean selected = parentProperties.window.getMouse().inField(textPos, new Vector2i(getSize().x, textGap.y));

                if (selected && mousePressed && !lastPress){ selectedComponent = component; }
                if (selectedComponent != null && !thisIsThatObject){ thisIsThatObject = selectedComponent == component; }

                Vector2i deleteTextPos = new Vector2i(getSize().x - 10,textGap.y * i + 29);
                boolean deleteTextSelected = parentProperties.window.getMouse().inField(new Vector2i(deleteTextPos.x + getPosition().x,deleteTextPos.y + getPosition().y), new Vector2i(10, textGap.y));

                if (deleteTextSelected && mousePressed && !lastPress){
                    toRemove = i;
                }


                freeTextDrawer.draw("-",deleteTextPos, deleteTextSelected ? textColor : highlightedTextColor);
                freeTextDrawer.draw(component.getName(), new Vector2i(0,textGap.y * i + 30), selected || selectedComponent == component ? highlightedTextColor : textColor);
            }

            lastPress = mousePressed;

            if (toRemove != -1) sceneObject.components.remove(toRemove);

            if (selectedComponent != null && !thisIsThatObject){
                selectedComponent = null;
            }

            if (SceneObjectComponentsPanel.selectedComponent == null){
                CameraMovement.accepted = true;
            }else if (SceneObjectComponentsPanel.selectedComponent.getName().equals("Rigidbody Component")){
                RuntimeFields.getFrameBuilder().addPart(new ShapesRenderFramePart(((RigidbodyComponent)SceneObjectComponentsPanel.selectedComponent).polygons,ObjectHighlight.getHighlightedObject()));

                CameraMovement.accepted = true;
            }

            canvas.update(partsBuilder, parentProperties);
        }
    }

}
