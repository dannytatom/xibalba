package me.dannytatom.xibalba;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import me.dannytatom.xibalba.screens.MainMenuScreen;

public class Main extends Game {
  public static final int SPRITE_WIDTH = 16;
  public static final int SPRITE_HEIGHT = 16;
  public static AssetManager assets;
  public static Skin skin;
  public static Screen playScreen;

  /**
   * From mouse position to tile position.
   *
   * @param camera Main world camera
   *
   * @return A vector2 of the hovered tile position
   */
  public static Vector2 mousePositionToWorld(OrthographicCamera camera) {
    Vector3 position = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    camera.unproject(position);

    return new Vector2(
        Math.round(position.x) / Main.SPRITE_WIDTH,
        Math.round(position.y) / Main.SPRITE_HEIGHT
    );
  }

  /**
   * Setup & load the main menu.
   */
  public void create() {
    // Load custom font
    assets = new AssetManager();
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal("ui/Aller_Rg.ttf"));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = 12;
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();

    // Create UI skin
    skin = new Skin();
    skin.add("Inconsolata", font, BitmapFont.class);
    skin.addRegions(new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas")));
    skin.load(Gdx.files.internal("ui/uiskin.json"));
    skin.getFont("default-font").getData().markupEnabled = true;

    // Set cursor image
    Pixmap pm = new Pixmap(Gdx.files.internal("ui/cursor.png"));
    Cursor cursor = Gdx.graphics.newCursor(pm, 0, 0);
    Gdx.graphics.setCursor(cursor);

    // Setup text colors
    Colors.put("LIGHT_GRAY", parseColor("c2c2c2"));
    Colors.put("DARK_GRAY", parseColor("666666"));
    Colors.put("CYAN", parseColor("67C8CF"));
    Colors.put("RED", parseColor("D67474"));
    Colors.put("YELLOW", parseColor("E0DFB1"));
    Colors.put("GREEN", parseColor("67CF8B"));

    // Map background colors
    Colors.put("CAVE_BACKGROUND", parseColor("293033"));

    // Start the main menu
    setScreen(new MainMenuScreen(this));
  }

  /**
   * Hex to RGBA.
   *
   * @param hex The color to parse
   *
   * @return A new Color object
   */
  private Color parseColor(String hex) {
    String s1 = hex.substring(0, 2);
    int v1 = Integer.parseInt(s1, 16);
    float f1 = (float) v1 / 255f;
    String s2 = hex.substring(2, 4);
    int v2 = Integer.parseInt(s2, 16);
    float f2 = (float) v2 / 255f;
    String s3 = hex.substring(4, 6);
    int v3 = Integer.parseInt(s3, 16);
    float f3 = (float) v3 / 255f;
    return new Color(f1, f2, f3, 1);
  }
}