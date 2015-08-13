package me.dannytatom.xibalba;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import me.dannytatom.xibalba.screens.MainMenuScreen;

public class Main extends Game {
  public BitmapFont font;
  public AssetManager assets;
  public Screen playScreen;
  public Entity player;

  public boolean debug = true;
  public boolean executeTurn = false;

  /**
   * Initialize the asset manager and start the loading screen.
   */
  public void create() {
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal("ui/Inconsolata.ttf"));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = 16;
    font = generator.generateFont(parameter);
    generator.dispose();

    assets = new AssetManager();
    playScreen = null;
    player = null;

    Colors.put("CYAN", parseColor("5bb9c7"));
    Colors.put("RED", parseColor("cc4141"));
    Colors.put("LIGHT_GRAY", parseColor("999999"));
    Colors.put("DARK_GRAY", parseColor("666666"));

    setScreen(new MainMenuScreen(this));
  }

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