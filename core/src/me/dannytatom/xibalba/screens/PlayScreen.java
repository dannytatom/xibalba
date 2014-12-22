package me.dannytatom.xibalba.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.xibalba.Main;
import me.dannytatom.xibalba.components.*;
import me.dannytatom.xibalba.components.ai.TargetComponent;
import me.dannytatom.xibalba.factories.MobFactory;
import me.dannytatom.xibalba.map.CaveGenerator;
import me.dannytatom.xibalba.map.Cell;
import me.dannytatom.xibalba.map.Map;
import me.dannytatom.xibalba.systems.BrainSystem;
import me.dannytatom.xibalba.systems.MovementSystem;
import me.dannytatom.xibalba.systems.PlayerSystem;
import me.dannytatom.xibalba.systems.ai.AttackSystem;
import me.dannytatom.xibalba.systems.ai.TargetSystem;
import me.dannytatom.xibalba.systems.ai.WanderSystem;
import me.dannytatom.xibalba.utils.ComponentMappers;
import org.xguzm.pathfinding.grid.GridCell;

class PlayScreen implements Screen, InputProcessor {
  private static final int SPRITE_WIDTH = 24;
  private static final int SPRITE_HEIGHT = 24;

  private final Main game;
  private OrthographicCamera camera;
  private SpriteBatch batch;
  private Engine engine;
  private Entity player;
  private Map map;
  private MobFactory mobFactory;

  /**
   * Play Screen.
   *
   * @param main Instance of Main class
   */
  public PlayScreen(Main main) {
    game = main;
    engine = new Engine();
    batch = new SpriteBatch();

    // Setup input
    Gdx.input.setInputProcessor(this);

    // Setup factories
    mobFactory = new MobFactory(game.assets);

    // Generate cave
    CaveGenerator cave = new CaveGenerator(game.assets.get("sprites/cave.atlas"), 15, 15);
    map = new Map(engine, cave.map);

    // Setup engine (they're run in order added)
    engine.addSystem(new PlayerSystem());
    engine.addSystem(new BrainSystem(map));
    engine.addSystem(new WanderSystem(map));
    engine.addSystem(new TargetSystem(map));
    engine.addSystem(new AttackSystem(map));
    engine.addSystem(new MovementSystem(map));

    // Setup camera
    camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.update();

    // Add player entity
    Vector2 startingPosition = map.findPlayerStart();
    player = new Entity();
    player.add(new PlayerComponent());
    player.add(new PositionComponent((int) startingPosition.x, (int) startingPosition.y));
    player.add(new MovementComponent());
    player.add(new VisualComponent(game.assets.get("sprites/player.png")));
    player.add(new AttributesComponent(100, 5));
    engine.addEntity(player);

    // Spawn some spider monkeys
    for (int i = 0; i < 1; i++) {
      Vector2 pos = map.getRandomOpenPosition();

      engine.addEntity(mobFactory.spawn("spiderMonkey", (int) pos.x, (int) pos.y));
    }
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0, 0, 0, 1);

    if (game.executeTurn) {
      executeTurn(delta);
    }

    updateCamera();
    renderWorld();
  }

  /**
   * Don't update any entities until the player has
   * an action to take.
   *
   * @param delta Time since last frame
   */
  void executeTurn(float delta) {
    // Let the systems run!
    engine.update(delta);

    // Get all entities with energy to spend
    ImmutableArray<Entity> entities =
        engine.getEntitiesFor(Family.all(AttributesComponent.class).get());

    // Give energy back
    for (Entity entity : entities) {
      AttributesComponent attributes = ComponentMappers.attributes.get(entity);
      attributes.energy += attributes.speed;
    }

    // Turn over
    game.executeTurn = false;
  }

  void updateCamera() {
    // Get player position for camera
    PositionComponent playerPosition = player.getComponent(PositionComponent.class);

    // Update camera
    camera.position.set(playerPosition.x * SPRITE_WIDTH, playerPosition.y * SPRITE_HEIGHT, 0);
    camera.update();
  }

  void renderWorld() {
    batch.setProjectionMatrix(camera.combined);

    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin();

    for (int x = 0; x < map.width; x++) {
      for (int y = 0; y < map.height; y++) {
        Cell cell = map.getCell(x, y);

        batch.draw(cell.sprite, x * SPRITE_WIDTH, y * SPRITE_HEIGHT);
      }
    }

    if (game.debug) {
      renderDebug();
    }

    // Iterate entities with a Position & Visual component
    // and draw them
    ImmutableArray<Entity> entities =
        engine.getEntitiesFor(Family.all(PositionComponent.class, VisualComponent.class).get());

    for (Entity entity : entities) {
      PositionComponent position = ComponentMappers.position.get(entity);
      VisualComponent visual = ComponentMappers.visual.get(entity);

      batch.draw(visual.sprite, position.x * SPRITE_WIDTH,
          (position.y * SPRITE_HEIGHT) + (SPRITE_HEIGHT / 2));
    }

    batch.end();
  }

  void renderDebug() {
    ImmutableArray<Entity> entities =
        engine.getEntitiesFor(Family.all(MovementComponent.class).get());

    for (Entity entity : entities) {
      MovementComponent movement = ComponentMappers.movement.get(entity);

      if (movement.path != null) {
        Texture texture;

        if (entity.getComponent(TargetComponent.class) != null) {
          texture = game.assets.get("sprites/utils/target.png");
        } else {
          texture = game.assets.get("sprites/utils/wander.png");
        }

        for (GridCell cell : movement.path) {
          batch.draw(texture, cell.getX() * SPRITE_WIDTH, cell.getY() * SPRITE_HEIGHT);
        }
      }
    }
  }

  @Override
  public void resize(int width, int height) {

  }

  @Override
  public void show() {

  }

  @Override
  public void hide() {

  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void dispose() {
    batch.dispose();
  }

  @Override
  public boolean keyDown(int keyCode) {
    MovementComponent movement = player.getComponent(MovementComponent.class);
    PositionComponent position = player.getComponent(PositionComponent.class);

    switch (keyCode) {
      case Input.Keys.BACKSLASH:
        game.debug ^= true;
        break;
      case Input.Keys.SPACE:
        game.executeTurn = true;
        break;
      case Input.Keys.K:
        movement.position = new Vector2(position.x, position.y + 1);
        game.executeTurn = true;
        break;
      case Input.Keys.U:
        movement.position = new Vector2(position.x + 1, position.y + 1);
        game.executeTurn = true;
        break;
      case Input.Keys.L:
        movement.position = new Vector2(position.x + 1, position.y);
        game.executeTurn = true;
        break;
      case Input.Keys.N:
        movement.position = new Vector2(position.x + 1, position.y - 1);
        game.executeTurn = true;
        break;
      case Input.Keys.J:
        movement.position = new Vector2(position.x, position.y - 1);
        game.executeTurn = true;
        break;
      case Input.Keys.B:
        movement.position = new Vector2(position.x - 1, position.y - 1);
        game.executeTurn = true;
        break;
      case Input.Keys.H:
        movement.position = new Vector2(position.x - 1, position.y);
        game.executeTurn = true;
        break;
      case Input.Keys.Y:
        movement.position = new Vector2(position.x - 1, position.y + 1);
        game.executeTurn = true;
        break;
      default:
    }

    return true;
  }

  @Override
  public boolean keyUp(int keyCode) {
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }
}
