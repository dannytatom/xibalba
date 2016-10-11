package me.dannytatom.xibalba.systems.statuses;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.components.statuses.BleedingComponent;
import me.dannytatom.xibalba.systems.UsesEnergySystem;
import me.dannytatom.xibalba.utils.ComponentMappers;
import me.dannytatom.xibalba.world.WorldManager;

public class BleedingSystem extends UsesEnergySystem {
  public BleedingSystem() {
    super(Family.all(BleedingComponent.class, AttributesComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    BleedingComponent bleeding = ComponentMappers.bleeding.get(entity);
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    if (bleeding.counter == bleeding.life) {
      entity.remove(BleedingComponent.class);
    } else {
      WorldManager.entityHelpers.takeDamage(entity, 5);

      if (WorldManager.entityHelpers.canSee(WorldManager.player, entity)) {
        boolean isPlayer = ComponentMappers.player.has(entity);

        WorldManager.log.add((isPlayer ? "You" : attributes.name) + " took 5 damage from bleeding");

        if (attributes.health <= 0) {
          WorldManager.log.add((isPlayer ? "[RED]You" : "[GREEN]" + attributes.name) + " bled to death");
        }
      }

      bleeding.counter += 1;
    }
  }
}
