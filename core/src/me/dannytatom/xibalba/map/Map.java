package me.dannytatom.xibalba.map;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.xibalba.components.PlayerComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.utils.ComponentMappers;
import org.xguzm.pathfinding.grid.GridCell;

public class Map {
  public final int width;
  public final int height;
  private final Engine engine;
  private final Cell[][] map;

  /**
   * Holds logic for dealing with maps.
   *
   * @param engine The Ashley engine
   * @param map    The actual map
   */
  public Map(Engine engine, Cell[][] map) {
    this.engine = engine;
    this.map = map;

    this.width = this.map.length;
    this.height = this.map[0].length;
  }

  /**
   * Get the cell for this pos
   *
   * @param x x pos of cell
   * @param y y pos of cell
   * @return The Cell instance at this pos
   */
  public Cell getCell(int x, int y) {
    return map[x][y];
  }

  /**
   * Find player pos.
   *
   * @return Vector2 of player pos
   */
  public Vector2 getPlayerPosition() {
    Entity player = engine.getEntitiesFor(Family.one(PlayerComponent.class).get()).first();
    PositionComponent position = ComponentMappers.position.get(player);

    return position.pos;
  }

  boolean isBlocked(int x, int y) {
    boolean blocked = map[x][y].isWall;

    if (!blocked) {
      ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(PositionComponent.class).get());

      for (Entity entity : entities) {
        PositionComponent position = ComponentMappers.position.get(entity);

        if (position.pos.x == x && position.pos.y == y) {
          blocked = true;
          break;
        }
      }
    }

    return blocked;
  }

  public boolean isWalkable(int x, int y) {
    return !isBlocked(x, y);
  }

  /**
   * Check if something is near the player.
   *
   * @param x        x of cell to check
   * @param y        y of cell to check
   * @param distance distance around cell to look
   * @return whether we're near the player or not
   */
  public boolean isNearPlayer(Vector2 position, int distance) {
    Vector2 playerPosition = getPlayerPosition();

    return position.x <= playerPosition.x + distance
        && position.x >= playerPosition.x - distance
        && position.y <= playerPosition.y + distance
        && position.y >= playerPosition.y - distance;
  }

  public GridCell[][] createGridCells() {
    GridCell[][] cells = new GridCell[width][height];

    for (int x = 0; x < map.length; x++) {
      for (int y = 0; y < map[x].length; y++) {
        cells[x][y] = new GridCell(x, y, isWalkable(x, y));
      }
    }

    return cells;
  }

  /**
   * Find a random open cell
   *
   * @return Random open cell
   */
  public Vector2 getRandomOpenPosition() {
    int x, y;

    do {
      x = MathUtils.random(0, map.length - 1);
      y = MathUtils.random(0, map[x].length - 1);
    } while (isBlocked(x, y));

    return new Vector2(x, y);
  }

  /**
   * Find player start.
   *
   * @return First open cell
   */
  public Vector2 findPlayerStart() {
    Vector2 space = new Vector2();

    search:
    for (int x = 0; x < map.length; x++) {
      for (int y = 0; y < map[x].length; y++) {
        if (isWalkable(x, y)) {
          space.add(x, y);

          break search;
        }
      }
    }

    return space;
  }
}