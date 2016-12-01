/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.healingblocks;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.healingblocks.component.HealingBlockComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.healingblocks.component.HealingBlockBuffComponent;
import org.terasology.logic.health.DoHealEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class HealingSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private BlockEntityRegistry blockEntityProvider;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    //go through all entities with a healing component and do the healing
    @Override
    public void update(float delta) {

        //iterate through all the entities with a location and healing component
        for (EntityRef entity : entityManager.getEntitiesWith(
                LocationComponent.class,
                HealingBlockBuffComponent.class,
                HealthComponent.class)) {


            //pull the healingBlockBuffComponent for easy access
            HealingBlockBuffComponent healingComponent = entity.getComponent(HealingBlockBuffComponent.class);

            //whether this should be in the loop or not is up for debate
            long timeInMs = time.getGameTimeInMs();

            //if it's passed nextHealTime
            if (healingComponent.nextHealTime <= timeInMs) {

                //heal for 1, with the source being the healing block
                entity.send(new DoHealEvent(
                        1,
                        blockEntityProvider.getBlockEntityAt(entity.getComponent(LocationComponent.class).getWorldPosition())));

                //sets next heal time based on the damage per second because damage has to be whole numbered
                //this is imprecise because it's an int, but more precise because it can deal with deltas that aren't exactly 1
                healingComponent.nextHealTime = timeInMs + (1000 / (healingComponent.healPerSecond));
            }

            //update the component
            entity.saveComponent(healingComponent);
        }
    }

    //TODO make sure this catches jumping onto a block too
    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent enterBlockEvent, EntityRef entity) {

        Vector3f newBlockPos = entity.getComponent(LocationComponent.class).getWorldPosition();
        newBlockPos.y--; //the position of the block under the player (based on CharacterSoundSystem's code)

        EntityRef newBlockEntity = blockEntityProvider.getBlockEntityAt(newBlockPos);

        HealingBlockComponent newBlockHealingComponent = newBlockEntity.getComponent(HealingBlockComponent.class);

        //if the entity was being healed by a healingBlock before moving
        if (entity.hasComponent(HealingBlockBuffComponent.class )) {

            //get rid of that healing component
            entity.removeComponent(HealingBlockBuffComponent.class);
        }

        //now if the new block is a healing block
        if (newBlockEntity.hasComponent(HealingBlockComponent.class)){

            //start healing it based won the block's healPerSecond value
            entity.addComponent(new HealingBlockBuffComponent(newBlockHealingComponent.healPerSecond));
        }
    }
}