import greenfoot.*;

/**
 * Базовый класс машины.
 * Содержит физику: скорость, ускорение, торможение, инерция поворота.
 *
 * Наследники: PlayerCar, BotCar
 */
public abstract class Car extends Actor {

    // --- Физические константы ---
    protected static final double MAX_SPEED        = 2.5;   // макс. скорость (пикс/кадр)
    protected static final double ACCELERATION     = 0.09;  // ускорение при газе
    protected static final double BRAKE_FORCE      = 0.12;  // замедление при тормозе
    protected static final double FRICTION         = 0.03;  // пассивное трение (без газа)
    protected static final double OFFROAD_DRAG     = 0.09;  // замедление при выезде за трассу
    protected static final double TURN_SPEED       = 3.5;   // градусов поворота в кадр
    protected static final double TURN_SPEED_SLOW  = 2.0;   // поворот на малой скорости

    // --- Состояние машины ---
    protected double speed      = 0.0;   // текущая скорость
    protected double angle      = 0.0;   // направление (градусы, 0 = вверх)
    protected boolean offRoad   = false; // выехал ли за трассу

    // --- Статистика (для менеджера гонки) ---
    protected int   currentLap        = 0;
    protected int   checkpointsPassed = 0;
    protected long  raceTimeMs        = 0;
    protected int   position          = 0; // место в гонке

    // ---------------------------------------------------------------
    //  Физика
    // ---------------------------------------------------------------

    /**
     * Применяет газ: увеличивает скорость до MAX_SPEED.
     */
    protected void accelerate() {
        speed += ACCELERATION;
        if (speed > MAX_SPEED) speed = MAX_SPEED;
    }

    /**
     * Применяет торможение / задний ход.
     */
    protected void brake() {
        speed -= BRAKE_FORCE;
        if (speed < -MAX_SPEED * 0.4) speed = -MAX_SPEED * 0.4; // огр. задний ход
    }

    /**
     * Пассивное замедление (трение), вызывается каждый кадр.
     */
    protected void applyFriction() {
        if (speed > 0) {
            speed -= offRoad ? OFFROAD_DRAG : FRICTION;
            if (speed < 0) speed = 0;
        } else if (speed < 0) {
            speed += offRoad ? OFFROAD_DRAG : FRICTION;
            if (speed > 0) speed = 0;
        }
    }

    /**
     * Поворачивает машину влево.
     * Скорость поворота зависит от текущей скорости.
     */
    protected void turnLeft() {
        if (Math.abs(speed) > 0.3) {
            double t = (Math.abs(speed) > 2.0) ? TURN_SPEED : TURN_SPEED_SLOW;
            angle -= t;
            setRotation((int) angle);
        }
    }

    /**
     * Поворачивает машину вправо.
     */
    protected void turnRight() {
        if (Math.abs(speed) > 0.3) {
            double t = (Math.abs(speed) > 2.0) ? TURN_SPEED : TURN_SPEED_SLOW;
            angle += t;
            setRotation((int) angle);
        }
    }

    /**
     * Перемещает машину по вектору текущего угла и скорости.
     * Вызывается после всех изменений скорости/угла.
     */
    protected void move() {
        double rad = Math.toRadians(angle - 90); // -90: в Greenfoot 0 = вправо, нам нужно вверх
        double dx  = Math.cos(rad) * speed;
        double dy  = Math.sin(rad) * speed;

        int newX = getX() + (int) Math.round(dx);
        int newY = getY() + (int) Math.round(dy);

        // Не выходить за границы мира
        int w = getWorld().getWidth();
        int h = getWorld().getHeight();
        newX = Math.max(0, Math.min(newX, w - 1));
        newY = Math.max(0, Math.min(newY, h - 1));

        setLocation(newX, newY);
    }

    /**
     * Главный метод физики — вызывать в act() каждого подкласса.
     * Применяет трение и перемещает машину.
     */
    protected void updatePhysics() {
        applyFriction();
        move();
    }

    // ---------------------------------------------------------------
    //  Столкновения
    // ---------------------------------------------------------------

    /**
     * Проверяет столкновение с другими машинами и отталкивает.
     */
    protected void handleCarCollisions() {
        Actor other = getOneIntersectingObject(Car.class);
        if (other != null && other != this) {
            // Простое отталкивание: откатиться назад
            speed *= -0.4;
            double rad = Math.toRadians(angle - 90);
            int pushX = getX() - (int) Math.round(Math.cos(rad) * 4);
            int pushY = getY() - (int) Math.round(Math.sin(rad) * 4);
            setLocation(pushX, pushY);
        }
    }

    /**
     * Устанавливает флаг offRoad.
     * Вызывается из RaceWorld при проверке пикселя трассы.
     */
    public void setOffRoad(boolean value) {
        this.offRoad = value;
    }

    // ---------------------------------------------------------------
    //  Геттеры для RaceManager и LapTimer
    // ---------------------------------------------------------------

    public double getSpeed()            { return speed; }
    public int    getCurrentLap()       { return currentLap; }
    public int    getCheckpointsPassed(){ return checkpointsPassed; }
    public long   getRaceTimeMs()       { return raceTimeMs; }
    public int    getPosition()         { return position; }

    // ---------------------------------------------------------------
    //  Сеттеры для RaceManager
    // ---------------------------------------------------------------

    public void setCurrentLap(int lap)         { this.currentLap = lap; }
    public void setCheckpointsPassed(int cp)   { this.checkpointsPassed = cp; }
    public void setRaceTimeMs(long t)          { this.raceTimeMs = t; }
    public void setPosition(int pos)           { this.position = pos; }

    /**
     * Устанавливает начальный угол машины при старте.
     * Вызывается из RaceManager после addObject().
     * 0=вверх, 90=вправо, 180=вниз, 270=влево.
     */
    public void setStartAngle(int startAngle) {
        this.angle = startAngle;
        setRotation(startAngle);
    }

    // ---------------------------------------------------------------
    //  Абстрактный act() — каждый подкласс реализует сам
    // ---------------------------------------------------------------

    @Override
    public abstract void act();
}
