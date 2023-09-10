# Hidden mines plugin 
The plugin adds mines that have the ability to explode

### Configuration
The configuration is divided into two parts: **the database and the mines**:
```yml
database:
  filename: "database.db"

mine:
  ground:
    allow: true
    explosionPower: 2.5
    cosmetic: true
    adaptiveCosmetic: true
    breakBlocks: true
    fireBlocks: true

  hook:
    allow: true
    explosionPower: 1.0
    cosmetic: true
    breakBlocks: true
    fireBlocks: true
```
#### The database
`database.filename` - the name for database file

#### The mine
There are 2 types of mines: **ground mines and hook mines**. Both types of mines have similar parameters that perform the **same functionality**
<br><br>
`mine.[ground/hook].allow` - if false, then players are not allowed to craft and place a mine.
<br><br>
`mine.[ground/hook].explosionPower` - the force of the explosion. Be careful, if you write down too large a number then the plugin may disrupt the server.
<br><br>
`mine.[ground/hook].cosmetic` - if true, then the mine will be displayed visually
<br><br>
`mine.ground.adaptiveCosmetic` - if true, then the visual part of the mines will adapt relative to the block on which it is located
<br><br>
`mine.[ground/hook].breakBlocks` - if true, then the blocks will collapse as from a normal TNT explosion
<br><br>
`mine.[ground/hook].fireBlock` - if true, then the blocks can ignite after the explosion

### Commands
`/remove <detonate:[true/false]> <radius> <type:[ground/hook]> <max:optional>` - A command that allows you to remove an array of mines within a given radius

### Recipes of mines
#### Ground
<p>
    <img src="images/crafting_ground_mine.png" alt="img" width="200" height="100"/>
    <img src="images/ground_mine.png" alt="img" width="200" height="100"/>
</p>

#### Hook
<p>
    <img src="images/crafting_hook_mine.png" alt="img" width="200" height="100"/>
    <img src="images/hook_mine.png" alt="img" width="200" height="100"/>
</p>