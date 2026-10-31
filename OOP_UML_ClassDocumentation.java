/**
 * ============================================================================
 *  OOP UML CLASS DOCUMENTATION - Call of Duty Zombies 2D
 * ============================================================================
 *
 *  THIS FILE CONTAINS ONLY COMMENTS. THERE IS NO EXECUTABLE CODE.
 *
 *  Purpose:
 *    This file documents, for every class in the project:
 *      1. Each attribute, tagged with its visibility modifier.
 *      2. Each class-to-class relationship (Association, Aggregation,
 *         Composition, Inheritance).
 *
 *  It is intended as a reference mirroring the actual Java source files
 *  (Player.java, Zombie.java, Weapon.java, Entity.java, Game.java,
 *  GamePanel.java, Bullet.java, Map.java, Door.java, Window.java, and the
 *  supporting classes PackAPunch, Particle, GameState, Perk, MysteryBox,
 *  WallGun, WaveManager, HUD, PowerUp, InputHandler).
 *
 *  ----------------------------------------------------------------------------
 *  VISIBILITY MODIFIER LEGEND (UML style):
 *    +  public       - accessible from anywhere
 *    #  protected    - accessible within the class and its subclasses
 *    -  private      - accessible only within the same class
 *    /  derived      - value computed from other attributes (not stored on its own)
 *    ~  package      - package-private (no modifier; accessible within same package)
 *  ----------------------------------------------------------------------------
 *  RELATIONSHIP LEGEND:
 *    Association      - Class A uses or references Class B (field, parameter,
 *                       local variable, return type).
 *    Aggregation      - "Has-a" weak ownership (a collection; the parts can
 *                       exist independently of the whole).
 *    Composition      - "Has-a" strong ownership (the part is created and
 *                       destroyed together with the whole; part-of).
 *    Inheritance      - "Is-a" (Class A extends or implements Class B).
 * ============================================================================
 */



/*******************************************************************************
 * CLASS: Entity  (abstract base class)
 *******************************************************************************
 *
 * PURPOSE: Base class for all game entities (Player, Zombie, Bullet).
 * Provides common position, velocity, bounds, and health.
 *
 * ATTRIBUTES:
 *   # x                 : double        - Entity X position (protected)
 *   # y                 : double        - Entity Y position (protected)
 *   # velocityX         : double        - Horizontal velocity (protected)
 *   # velocityY         : double        - Vertical velocity (protected)
 *   # width             : double        - Entity width (protected)
 *   # height            : double        - Entity height (protected)
 *   # maxHealth         : double        - Maximum health (protected)
 *   # health            : double        - Current health (protected)
 *   # alive             : boolean       - Whether entity is alive (protected)
 *   # bounds            : Rectangle2D.Double - Collision bounds (protected)
 *
 *   / getHealthPercent() : double      - Derived: health / maxHealth
 *
 * RELATIONSHIPS:
 *   Inheritance:  Entity IS-A abstract base class; extended by Player,
 *                 Zombie, and Bullet.
 *
 *   (No Association / Aggregation / Composition fields of its own;
 *    it is the parent that other classes inherit from.)
 */



/*******************************************************************************
 * CLASS: Player  (extends Entity)
 *******************************************************************************
 *
 * PURPOSE: Represents the player character. Handles movement, shooting,
 * weapons, perks, stats, and the downed state.
 *
 * ATTRIBUTES:
 *   + MOVE_SPEED                : double (static final) - Base movement speed (250)
 *   # DIAGONAL_SPEED            : double (static final) - Diagonal movement speed
 *   - lastShotTime              : double     - Timestamp of last shot fired
 *   - shootCooldown             : double     - Cooldown remaining before next shot
 *   - points                    : int        - Player score / points
 *   - maxHealth                 : int        - Maximum health (with Juggernog)
 *   - baseMaxHealth             : int        - Base maximum health (100)
 *   - healthRegenTimer          : double     - Seconds since last hit (health regen)
 *   - damageMultiplier          : double     - Damage multiplier from perks
 *   - fireRateMultiplier        : double     - Fire rate multiplier from perks
 *   - reloadSpeedMultiplier     : double     - Reload speed multiplier from perks
 *   - moveSpeedMultiplier       : double     - Move speed multiplier from perks
 *   - primaryWeapon             : Weapon     - Primary equipped weapon
 *   - secondaryWeapon           : Weapon     - Secondary equipped weapon
 *   - thirdWeapon               : Weapon     - Third weapon (Mule Kick slot)
 *   - currentWeapon             : Weapon     - Currently active weapon
 *   - weaponSlot                : int        - Current weapon slot index
 *   - activePerks               : List<Perk> - List of active perks
 *   - hasJuggernog              : boolean    - Has Juggernog perk
 *   - hasSpeedCola              : boolean    - Has Speed Cola perk
 *   - hasDoubleTap              : boolean    - Has Double Tap perk
 *   - hasQuickRevive            : boolean    - Has Quick Revive perk
 *   - hasStaminUp               : boolean    - Has Stamin-Up perk
 *   - hasDeadshot               : boolean    - Has Deadshot Daiquiri perk
 *   - hasMuleKick               : boolean    - Has Mule Kick perk
 *   - hasPhD                    : boolean    - Has PhD Flopper perk
 *   - isDowned                  : boolean    - Whether player is downed
 *   - downedTime                : double     - Time remaining in downed state
 *   # DOWNED_BLEED_OUT_TIME     : double (static final) - Bleed-out duration (30s)
 *   - quickReviveTimer          : double     - Quick Revive auto-revive timer
 *   # QUICK_REVIVE_REVIVE_TIME  : double (static final) - Self-revive time (4s)
 *   - gamePanel                 : GamePanel  - Reference to the game panel
 *
 * RELATIONSHIPS:
 *   Inheritance:   Player IS-A Entity (extends Entity)
 *
 *   Composition:   Player HAS-A Weapon
 *                  (primaryWeapon, secondaryWeapon, thirdWeapon,
 *                   currentWeapon are created and owned by the Player)
 *
 *   Aggregation:   Player HAS-A List<Perk> activePerks
 *                  (perk collection; perks can exist independently)
 *
 *   Association:   Player USES GamePanel        (stored reference field)
 *   Association:   Player USES InputHandler     (method parameter for input)
 *   Association:   Player USES Map              (constructor param; collision)
 *   Association:   Player USES Bullet           (created when shooting)
 *   Association:   Player USES MysteryBox       (E/F interaction)
 *   Association:   Player USES PackAPunch       (E/F interaction)
 *   Association:   Player USES WallGun          (E/F interaction)
 *   Association:   Player USES Door             (E/F interaction)
 *   Association:   Player USES Perk             (E/F interaction)
 */



/*******************************************************************************
 * CLASS: Zombie  (extends Entity)
 *******************************************************************************
 *
 * PURPOSE: Zombie enemy with different types and AI behavior.
 * Includes pathfinding, window/door traversal, and attack logic.
 *
 * ATTRIBUTES (nested Type enum - all public final):
 *   + Type.health          : double   - Base health for this zombie type
 *   + Type.damage          : double   - Damage dealt to player on attack
 *   + Type.speedMultiplier : double   - Speed multiplier for this type
 *   + Type.pointsValue     : int      - Points awarded when this zombie dies
 *   + Type.color           : Color    - Render color for this type
 *   + Type.sizeMultiplier  : double   - Size multiplier for this type
 *
 * ATTRIBUTES (instance):
 *   # BASE_SPEED        : double (static final) - Base zombie speed (80)
 *   # ATTACK_RANGE      : double (static final) - Attack range (35)
 *   # ATTACK_COOLDOWN   : double (static final) - Attack cooldown (1 second)
 *   - type              : Type       - The zombie type (NORMAL, FAST, TANK, ...)
 *   - speed             : double     - Current movement speed
 *   - attackTimer       : double     - Timer until next attack is allowed
 *   - random            : Random     - Random number generator (AI spread)
 *   - stuckTimer        : double     - Time stuck in place (for recovery)
 *   - lastX, lastY      : double     - Last position (stuck detection)
 *   - playerRef         : Player     - Reference to the player (final field)
 *
 * RELATIONSHIPS:
 *   Inheritance:   Zombie IS-A Entity (extends Entity)
 *
 *   Association:   Zombie USES Player        (playerRef field; attackPlayer)
 *   Association:   Zombie USES Map           (method param; pathfinding/collision)
 *   Association:   Zombie USES List<Zombie>  (method param; separation AI)
 *   Association:   Zombie USES Window        (local var; pathfinding through gaps)
 *   Association:   Zombie USES Door          (local var; pathfinding through gaps)
 *
 *   (The nested Type enum is a composition-like part of Zombie's definition,
 *    but since it is a static nested enum it is treated as a type nested
 *    inside Zombie rather than a separate runtime object.)
 */



/*******************************************************************************
 * CLASS: Weapon
 *******************************************************************************
 *
 * PURPOSE: Represents all guns in the game. Includes stats, firing mechanics,
 * reloading, and Pack-a-Punch upgrades.
 *
 * ATTRIBUTES (nested Type enum - all public final):
 *   + Type.name            : String     - Display name of the weapon
 *   + Type.baseDamage      : double     - Base damage per shot
 *   + Type.magazineSize    : int        - Rounds per magazine
 *   + Type.maxAmmo         : int        - Maximum reserve ammo
 *   + Type.fireRate        : int        - Rounds per minute (RPM)
 *   + Type.reloadTime      : double     - Reload duration in seconds
 *   + Type.adsTime         : double     - Aim-down-sights time
 *   + Type.pointsCost      : int        - Cost to buy from the wall
 *   + Type.weaponClass     : WeaponClass - Category (PISTOL, SMG, ASSAULT, ...)
 *   + Type.color           : Color      - Render color for this weapon
 *   + Type.mysteryBoxCost  : int        - Cost from the mystery box
 *
 * ATTRIBUTES (nested WeaponClass enum - public constants):
 *   + PISTOL, SMG, ASSAULT, SHOTGUN, LMG, SNIPER, WONDER, SPECIAL
 *
 * ATTRIBUTES (instance):
 *   - type               : Type       - The weapon type
 *   - damage             : double     - Current damage (may be modified by PaP)
 *   - magazineSize       : int        - Current magazine size
 *   - maxAmmo            : int        - Current max reserve ammo
 *   - currentAmmo        : int        - Ammo currently in the magazine
 *   - reserveAmmo        : int        - Ammo in reserve
 *   - fireRate           : int        - Current fire rate (RPM)
 *   - reloadTime         : double     - Current reload time
 *   - adsTime            : double     - Current ADS time
 *   - pointsCost         : int        - Current points cost
 *   - weaponClass        : WeaponClass- Current weapon class
 *   - color              : Color      - Current render color
 *   - packAPunched       : boolean    - Whether Pack-a-Punched
 *   - customName         : String     - Custom display name
 *   - lastShotTime       : double     - Timestamp of last shot
 *   - shootCooldown      : double     - Cooldown before next shot
 *   - reloading          : boolean    - Whether currently reloading
 *   - reloadTimer        : double     - Reload timer counting down
 *   - random             : Random     - Random number generator (spread)
 *   - player             : Player     - Reference to owning player (perks)
 *
 *   / getReloadProgress() : double    - Derived: reload progress (0.0 to 1.0)
 *
 * RELATIONSHIPS:
 *   Association:   Weapon USES Player        (setPlayer / update take Player;
 *                                              perk multipliers applied to shots)
 *   Association:   Weapon USES Bullet        (created in the shoot() method)
 *   Association:   Weapon USES WeaponClass   (enum type field)
 *   Association:   Weapon USES Color         (render color field)
 *
 *   (Type and WeaponClass are nested static enums inside Weapon — they are
 *    types defined within Weapon, not separate runtime objects.)
 */



/*******************************************************************************
 * CLASS: Bullet  (extends Entity)
 *******************************************************************************
 *
 * PURPOSE: Bullet / projectile for player and zombie weapons.
 * Moves in a straight line, expires after maxLifeTime, supports piercing.
 *
 * ATTRIBUTES:
 *   - damage        : double   - Damage dealt on hit
 *   - angle         : double   - Travel angle in radians
 *   - speed         : double   - Movement speed
 *   - lifeTime      : double   - Time alive so far
 *   - maxLifeTime   : double   - Max lifetime before expiry (3.0 seconds)
 *   - fromZombie    : boolean  - Whether fired by a zombie
 *   - critical      : boolean  - Whether this is a critical hit
 *   - color         : Color    - Render color
 *   - piercing      : boolean  - Whether bullet can pierce multiple targets
 *   - pierceCount   : int      - Number of targets pierced so far
 *   - maxPierce     : int      - Max number of pierces allowed
 *
 * RELATIONSHIPS:
 *   Inheritance:   Bullet IS-A Entity (extends Entity)
 *
 *   Association:   Bullet USES Color   (render color field)
 */



/*******************************************************************************
 * CLASS: Game
 *******************************************************************************
 *
 * PURPOSE: Main entry point. Initializes the JFrame and starts the game loop.
 *
 * ATTRIBUTES:
 *   + WIDTH   : int (static final) - Window width (1280)
 *   + HEIGHT  : int (static final) - Window height (720)
 *   + TITLE   : String (static final) - Window title
 *   + FPS     : int (static final) - Target frames per second (60)
 *   - frame          : JFrame     - The main window frame
 *   - gamePanel      : GamePanel  - The game panel (contains game logic)
 *
 * RELATIONSHIPS:
 *   Composition:   Game HAS-A GamePanel
 *                  (created in the Game constructor; owned by Game)
 *
 *   Association:   Game USES JFrame     (windowing)
 *   Association:   Game USES Game       (uses its own WIDTH/HEIGHT constants)
 */



/*******************************************************************************
 * CLASS: GamePanel  (extends JPanel, implements Runnable)
 *******************************************************************************
 *
 * PURPOSE: Main game panel. Handles the game loop, rendering, input, and
 * coordination of all game objects.
 *
 * ATTRIBUTES:
 *   # NANOSECONDS_PER_SECOND   : double (static final) - 1_000_000_000.0
 *   # TARGET_FPS               : double (static final) - 60.0
 *   # TARGET_TIME_PER_FRAME    : double (static final) - Nanoseconds per frame
 *   - player                   : Player      - The player object
 *   - map                      : Map         - The game map
 *   - waveManager              : WaveManager - Manages zombie waves
 *   - bullets                  : List<Bullet> - Active bullets
 *   - zombies                  : List<Zombie> - Active zombies
 *   - powerUps                 : List<PowerUp> - Active power-ups
 *   - particles                : List<Particle> - Active particles
 *   - mysteryBox               : MysteryBox  - The mystery box
 *   - packAPunch               : PackAPunch  - The pack-a-punch machine
 *   - wallGuns                 : List<WallGun> - Wall gun purchase points
 *   - doors                    : List<Door>  - Doors on the map
 *   - hud                      : HUD         - Heads-up display
 *   - inputHandler             : InputHandler- Keyboard/mouse input handler
 *   - gameState                : GameState   - Current game state (volatile)
 *   - gameThread               : Thread      - Game loop thread
 *   - running                  : boolean     - Whether game loop is running
 *   - cameraX, cameraY         : double      - Camera position
 *   - screenShakeIntensity     : double      - Current screen shake intensity
 *   - screenShakeDuration      : double      - Screen shake time remaining
 *   - backBuffer               : BufferedImage - Double-buffer image
 *   - backBufferGraphics       : Graphics2D  - Double-buffer graphics context
 *
 * RELATIONSHIPS:
 *   Inheritance:   GamePanel IS-A JPanel   (extends JPanel)
 *   Inheritance:   GamePanel IS-A Runnable (implements Runnable)
 *
 *   Composition:   GamePanel HAS-A Player        (created in initializeGame())
 *   Composition:   GamePanel HAS-A Map          (created in initializeGame())
 *   Composition:   GamePanel HAS-A WaveManager  (created in initializeGame())
 *   Composition:   GamePanel HAS-A MysteryBox   (created in initializeGame())
 *   Composition:   GamePanel HAS-A PackAPunch   (created in initializeGame())
 *   Composition:   GamePanel HAS-A HUD          (created in initializeGame())
 *   Composition:   GamePanel HAS-A InputHandler (created in initializeInput())
 *
 *   Aggregation:   GamePanel HAS-A List<Bullet>   (bullets come and go)
 *   Aggregation:   GamePanel HAS-A List<Zombie>   (zombies come and go)
 *   Aggregation:   GamePanel HAS-A List<PowerUp>  (power-ups come and go)
 *   Aggregation:   GamePanel HAS-A List<Particle> (particles come and go)
 *   Aggregation:   GamePanel HAS-A List<WallGun>  (obtained from Map)
 *   Aggregation:   GamePanel HAS-A List<Door>     (obtained from Map)
 *
 *   Association:   GamePanel USES GameState (field; game state machine)
 *   Association:   GamePanel USES Game      (constants WIDTH/HEIGHT)
 *   Association:   GamePanel USES Thread    (game loop thread)
 *   Association:   GamePanel USES BufferedImage / Graphics2D (double buffering)
 */



/*******************************************************************************
 * CLASS: Map
 *******************************************************************************
 *
 * PURPOSE: Game map with walls, floors, barriers, spawn points, perks,
 * wall guns, doors, and windows. Layout: 4 corner rooms + 1 central room.
 *
 * ATTRIBUTES:
 *   + WIDTH     : int (static final) - Map width (3000)
 *   + HEIGHT    : int (static final) - Map height (2000)
 *   + TILE_SIZE : int (static final) - Tile size (64)
 *   - walls                  : List<Rectangle2D.Double> - Wall collision rects
 *   - floorTiles             : List<Rectangle2D.Double> - Floor tile rects
 *   - perks                  : List<Perk>      - Perk machines on the map
 *   - wallGuns               : List<WallGun>   - Wall gun purchase points
 *   - doors                  : List<Door>      - Doors on the map
 *   - windows                : List<Window>    - Windows on the map
 *   - zombieSpawnPoints      : List<Point>     - Zombie spawn locations
 *   - playerSpawnPoints      : List<Point>     - Player spawn locations
 *   - random                 : Random          - Random number generator
 *
 * RELATIONSHIPS:
 *   Composition:   Map HAS-A Perk            (creates perk machines)
 *   Composition:   Map HAS-A WallGun         (creates wall guns)
 *   Composition:   Map HAS-A Door            (creates doors)
 *   Composition:   Map HAS-A Window          (creates windows)
 *   Composition:   Map HAS-A Point           (spawn point locations)
 *
 *   Association:   Map USES Rectangle2D.Double (walls and floor tiles)
 *   Association:   Map USES Random           (spawn point selection)
 */



/*******************************************************************************
 * CLASS: Door
 *******************************************************************************
 *
 * PURPOSE: Purchasable doorway. Players open doors by spending points.
 * Doors start closed, block movement, and open permanently once purchased.
 *
 * ATTRIBUTES:
 *   - x       : double - Door X position
 *   - y       : double - Door Y position
 *   - width   : double - Door width
 *   - height  : double - Door height
 *   - bounds  : Rectangle2D.Double - Collision bounds
 *   - cost    : int    - Points cost to open the door
 *   - opened  : boolean- Whether the door is opened
 *   - label   : String - Display label for the door
 *
 * RELATIONSHIPS:
 *   Association:   Door USES Player           (method parameter:
 *                                               isPlayerInRange, attemptPurchase)
 *   Association:   Door USES Rectangle2D.Double (bounds field)
 */



/*******************************************************************************
 * CLASS: Window
 *******************************************************************************
 *
 * PURPOSE: Virtual window opening in a room wall. Blocks player movement
 * but zombies can pass through freely.
 *
 * ATTRIBUTES:
 *   - x       : double - Window X position
 *   - y       : double - Window Y position
 *   - width   : double - Window width
 *   - height  : double - Window height
 *   - bounds  : Rectangle2D.Double - Collision bounds
 *
 * RELATIONSHIPS:
 *   Association:   Window USES Rectangle2D.Double (bounds field)
 */



/*******************************************************************************
 * SUPPORTING CLASSES (brief reference)
 *******************************************************************************
 *
 *  The remaining classes in the project are supporting classes. They are
 *  referenced by the classes above and participate in the relationships
 *  noted here.
 *
 *  CLASS: PackAPunch
 *    - HAS-A (Composition) PackAPunch machine created/owned by GamePanel.
 *    - USES Player (method parameter for upgrade attempts).
 *    - Attributes (inferred): position, size, bounds, cost, isActive/upgraded flag.
 *
 *  CLASS: MysteryBox
 *    - HAS-A (Composition) MysteryBox created/owned by GamePanel.
 *    - USES Player (method parameter for purchase attempts).
 *    - Attributes (inferred): position, size, bounds, currentWeapon, isOpen flag.
 *
 *  CLASS: WallGun
 *    - HAS-A (Composition) WallGun instances created by Map, held by GamePanel.
 *    - USES Player (method parameter for purchase attempts).
 *    - Attributes (inferred): position, weapon type, cost, isPurchased flag.
 *
 *  CLASS: WaveManager
 *    - HAS-A (Composition) WaveManager created/owned by GamePanel.
 *    - USES Map and Player (constructor parameters).
 *    - USES List<Zombie> (method parameter for spawning).
 *    - Attributes (inferred): current wave number, zombie count per wave.
 *
 *  CLASS: HUD
 *    - HAS-A (Composition) HUD created/owned by GamePanel.
 *    - USES Player and WaveManager (constructor parameters).
 *    - Attributes (inferred): power-up timers (instaKill, doublePoints, etc.).
 *
 *  CLASS: InputHandler
 *    - HAS-A (Composition) InputHandler created/owned by GamePanel.
 *    - USED BY Player (method parameter for movement/shooting input).
 *    - Attributes (inferred): key states, mouse position, button states.
 *
 *  CLASS: PowerUp
 *    - AGGREGATED (List<PowerUp>) by GamePanel.
 *    - USES Player (collect method applies effect to player).
 *    - Attributes (inferred): position, type, isCollected, isExpired, color.
 *
 *  CLASS: Perk
 *    - AGGREGATED (List<Perk>) by Player (activePerks) and Map (perks list).
 *    - USES Player (attemptPurchase applies perk to player).
 *    - Attributes (inferred): position, type, color, isPurchased flag.
 *
 *  CLASS: Particle
 *    - AGGREGATED (List<Particle>) by GamePanel.
 *    - Attributes (inferred): position, velocity, color, lifeTime, isAlive.
 *
 *  CLASS: GameState
 *    - USED AS a field type by GamePanel (gameState).
 *    - Likely an enum: MAIN_MENU, PLAYING, PAUSED, GAME_OVER.
 *
 *  CLASS: Bullet   -> already documented above (extends Entity).
 *  CLASS: Entity   -> already documented above (abstract base class).
 *  CLASS: Game     -> already documented above (main entry point).
 *  CLASS: GamePanel-> already documented above (main game panel).
 *  CLASS: Map      -> already documented above (game map).
 *  CLASS: Player   -> already documented above (extends Entity).
 *  CLASS: Weapon   -> already documented above.
 *  CLASS: Window   -> already documented above.
 *  CLASS: Door     -> already documented above.
 *  CLASS: Zombie   -> already documented above (extends Entity).
 */



/*******************************************************************************
 * CLASS-TO-CLASS RELATIONSHIP SUMMARY
 *******************************************************************************
 *
 *  INHERITANCE ("Is-a"):
 *    Player   extends Entity
 *    Zombie   extends Entity
 *    Bullet   extends Entity
 *    GamePanel extends JPanel
 *    GamePanel implements Runnable
 *
 *  COMPOSITION ("Has-a", strong ownership, created/destroyed together):
 *    Game        -> GamePanel
 *    GamePanel   -> Player, Map, WaveManager, MysteryBox, PackAPunch, HUD,
 *                    InputHandler
 *    Map         -> Perk, WallGun, Door, Window, Point (spawn points)
 *    Player      -> Weapon (primary, secondary, third, current)
 *
 *  AGGREGATION ("Has-a", weak ownership via collections):
 *    GamePanel   -> List<Bullet>, List<Zombie>, List<PowerUp>,
 *                   List<Particle>, List<WallGun>, List<Door>
 *    Map         -> List<Perk>, List<WallGun>, List<Door>, List<Window>
 *    Player      -> List<Perk> (activePerks)
 *
 *  ASSOCIATION ("Uses", references via fields/parameters/locals):
 *    Player      -> GamePanel, InputHandler, Map, Bullet, MysteryBox,
 *                   PackAPunch, WallGun, Door, Perk
 *    Zombie      -> Player, Map, List<Zombie>, Window, Door
 *    Weapon      -> Player, Bullet, WeaponClass, Color
 *    GamePanel   -> GameState, Game, Thread, BufferedImage, Graphics2D
 *    Map         -> Rectangle2D.Double, Random
 *    Door        -> Player, Rectangle2D.Double
 *    Window      -> Rectangle2D.Double
 *    Bullet      -> Color
 *    PowerUp     -> Player
 *    Perk        -> Player
 *    WaveManager -> Map, Player, List<Zombie>
 *    HUD         -> Player, WaveManager
 *    MysteryBox  -> Player
 *    PackAPunch  -> Player
 *    WallGun     -> Player
 *    InputHandler-> (used by Player)
 *
 * ============================================================================
 *  END OF DOCUMENTATION (comments only - no code)
 * ============================================================================
 */
