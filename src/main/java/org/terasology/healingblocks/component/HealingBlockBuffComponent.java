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

package org.terasology.healingblocks.component;

import org.terasology.entitySystem.Component;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;

public final class HealingBlockBuffComponent implements Component{
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public int healPerSecond;
    public float nextHealTime = 0.0f;

    public HealingBlockBuffComponent(){
        healPerSecond = 1;
    }

    public HealingBlockBuffComponent(int amountToHealPerSecond) {
        healPerSecond = amountToHealPerSecond;
    }
}


