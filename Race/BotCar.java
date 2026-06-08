import greenfoot.*;

/**
 * Машина бота. Сложность фиксирована: MEDIUM.
 * Спрайт задаётся через конструктор.
 */
public class BotCar extends Car {

    public static final int EASY   = 0;
    public static final int MEDIUM = 1;
    public static final int HARD   = 2;

    private final int   difficulty;
    private final BotAI ai;
    private final double speedMultiplier;

    // ---------------------------------------------------------------
    //  Конструкторы
    // ---------------------------------------------------------------

    public BotCar() {
        this(MEDIUM, 0, "Astonmartin.png");
    }

    public BotCar(int difficulty, int botIndex, String spriteFile) {
        this.difficulty = difficulty;
        this.ai         = new BotAI(this, difficulty);

        double spread = 0.04 * botIndex - 0.04;
        this.speedMultiplier = 1.0 + spread;

        setImage(spriteFile);
    }

    // ---------------------------------------------------------------
    //  Главный цикл
    // ---------------------------------------------------------------

    @Override
    public void act() {
        ai.update();
        applySpeedMultiplier();
        handleCarCollisions();
        updatePhysics();
    }

    private void applySpeedMultiplier() {
        double effectiveMax = MAX_SPEED * speedMultiplier;
        if (speed > effectiveMax) speed = effectiveMax;
    }

    // ---------------------------------------------------------------
    //  Геттеры
    // ---------------------------------------------------------------

    public int    getDifficulty() { return difficulty; }
    public double getCarAngle()   { return angle; }
}
