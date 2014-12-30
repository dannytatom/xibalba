package me.dannytatom.xibalba.systems.actions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.actions.MovementComponent;
import me.dannytatom.xibalba.map.Map;
import me.dannytatom.xibalba.utils.ComponentMappers;

import java.util.Comparator;

public class MovementSystem extends SortedIteratingSystem {
  private final Map map;

  /**
   * System to control movement of entities.
   *
   * @param map the map we're moving on
   */
  public MovementSystem(Map map) {
    super(Family.all(PositionComponent.class, MovementComponent.class,
        AttributesComponent.class).get(), new EnergyComparator());

    this.map = map;
  }

  /**
   * If the entities have a move action in queue,
   * and can move where they're wanting to,
   * move 'em.
   *
   * @param entity    The entity to process
   * @param deltaTime Time since last frame
   */
  public void processEntity(Entity entity, float deltaTime) {
    PositionComponent position = ComponentMappers.position.get(entity);
    MovementComponent movement = ComponentMappers.movement.get(entity);
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    if (movement.pos != null && map.isWalkable(movement.pos)) {
      position.pos = movement.pos;
      attributes.energy -= MovementComponent.COST;
    }

    entity.remove(MovementComponent.class);
  }

  private static class EnergyComparator implements Comparator<Entity> {
    @Override
    public int compare(Entity e1, Entity e2) {
      AttributesComponent a1 = e1.getComponent(AttributesComponent.class);
      AttributesComponent a2 = e2.getComponent(AttributesComponent.class);

      if (a2.energy > a1.energy) {
        return 1;
      } else if (a1.energy > a2.energy) {
        return -1;
      } else {
        return 0;
      }
    }
  }
}
