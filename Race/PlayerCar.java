import greenfoot.*;

/**
 * Машина игрока.
 * Спрайт задаётся через конструктор (выбирается на экране TeamSelect).
 */
public class PlayerCar extends Car {

    public static final int SCHEME_ARROWS = 0;
    public static final int SCHEME_WASD   = 1;

    private final int controlScheme;
    private final int playerNumber;

    // ---------------------------------------------------------------
    //  Конструкторы
    // ---------------------------------------------------------------

    public PlayerCar() {
        this(SCHEME_ARROWS, 1, "Ferrari.png");
    }

    public PlayerCar(int scheme, int playerNumber, String spriteFile) {
        this.controlScheme = scheme;
        this.playerNumber  = playerNumber;
        setImage(spriteFile);
    }

    // ---------------------------------------------------------------
    //  Главный цикл
    // ---------------------------------------------------------------

    @Override
    public void act() {
        readInput();
        handleCarCollisions();
        updatePhysics();
        debugShowSpeed(); // ВРЕМЕННО: показываем speed для диагностики
    }

    /** ВРЕМЕННЫЙ метод для отладки — удалить после диагностики. */
    private void debugShowSpeed() {
        if (getWorld() == null) return;
        getWorld().showText("speed=" + String.format("%.2f", speed) +
                             " angle=" + String.format("%.0f", angle) +
                             " x=" + getX() + " y=" + getY(), 200, 30);
    }

    // ---------------------------------------------------------------
    //  Ввод
    // ---------------------------------------------------------------

    private void readInput() {
        if (controlScheme == SCHEME_ARROWS) {
            if (Greenfoot.isKeyDown("up"))    accelerate();
            if (Greenfoot.isKeyDown("down"))  brake();
            if (Greenfoot.isKeyDown("left"))  turnLeft();
            if (Greenfoot.isKeyDown("right")) turnRight();
        } else {
            if (Greenfoot.isKeyDown("w")) accelerate();
            if (Greenfoot.isKeyDown("s")) brake();
            if (Greenfoot.isKeyDown("a")) turnLeft();
            if (Greenfoot.isKeyDown("d")) turnRight();
        }
    }

    // ---------------------------------------------------------------
    //  Геттеры
    // ---------------------------------------------------------------

    public int getPlayerNumber()  { return playerNumber; }
    public int getControlScheme() { return controlScheme; }
}
