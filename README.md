## HealingBlocks
This is a HealingBlocks module for Terasology (https://github.com/MovingBlocks/Terasology)
#Features
This module contains a test block called RedCross that heals players for 1 health per second while they stand on it. It also provides support for making many more healing blocks, that heal at different speeds, using the `HealingBlock` component.
#Structure
In order to allow for many players and different healing amounts, the `HealingSystem` gives any entity that steps on healing block a `HealingBlockBuffComponent`, and removes it if they step off of the block. If they step onto another healing block, the `HealingSystem` just updates the component to reflect the new healing rate, to prevent rapid healing by moving back and forth between healing blocks. The system also heals players with a `HealingBlockBuffComponent` at the rated given by the variable `healPerSecond`.
#Making a HealingBlock
To make a healing block, simply add a `HealingBlock` component to the block's prefab file, and set a float value for `healPerSecond`.
