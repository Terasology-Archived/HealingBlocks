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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.healingblocks.component.HealingBlockComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.healingblocks.component.HealingBlockBuffComponent;
import org.terasology.logic.health.DoHealEvent;

public class HealingSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    //private static final Logger logger = LoggerFactory.getLogger(HealingSystem.class); //use logger.info(string) to send to console

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

            //pull the healingBlockBuffcomponent for easy access
            HealingBlockBuffComponent healingComponent = entity.getComponent(HealingBlockBuffComponent.class);

            //whether this should be in the loop or not is up for debate
            long timeInMs = time.getGameTimeInMs();

            //if it's been more than one second since the last heal
            if (healingComponent.nextHealTime <= timeInMs) {

                //heal for the amount per second, with the source being the healing block
                entity.send(new DoHealEvent(
                        healingComponent.healPerSecond,
                        blockEntityProvider.getBlockEntityAt(entity.getComponent(LocationComponent.class).getWorldPosition())));
            }

            //heal again, a second later (accounts for delta time potentially not being exactly 1s
            healingComponent.nextHealTime += 1000;

            //update the component
            entity.saveComponent(healingComponent);
        }
    }

    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent enterBlockEvent, EntityRef entity) {
        Vector3f newBlockPos = entity.getComponent(LocationComponent.class).getWorldPosition();
        newBlockPos.y--; //the position of the block under the player (based on CharacterSoundSystem's code)

        EntityRef newBlockEntity = blockEntityProvider.getBlockEntityAt(newBlockPos);

        HealingBlockComponent newBlockHealingComponent = newBlockEntity.getComponent(HealingBlockComponent.class);

        //if the entity was being healed by a healingblock before moving
        if (entity.hasComponent(HealingBlockBuffComponent.class )) {

            //get rid of that healing component
            entity.removeComponent(HealingBlockBuffComponent.class);
        }

        //now if the new block is a healing block
        if (!newBlockHealingComponent.equals(null)){

            //start healing it based on the block's healPerSecond value
            entity.addComponent(new HealingBlockBuffComponent(newBlockHealingComponent.healPerSecond));
        }
    }
}