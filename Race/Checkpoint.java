import greenfoot.*;

/**
 * Чекпоинт (контрольная точка) на трассе.
 *
 * Реализация разработчика Б:
 *   - Невидимый актор (прозрачное изображение 40x40).
 *   - Каждый кадр проверяет пересечение с машиной.
 *   - При пересечении уведомляет LapTimer через RaceManager.
 *   - Антишорткат реализован в LapTimer (принимает только ожидаемый order).
 */
public class Checkpoint extends Actor {

    /** Размер зоны срабатывания чекпоинта (пиксели). */
    private static final int TRIGGER_SIZE = 40;

    private int order; // порядковый номер: 0, 1, 2, ...

    // ---------------------------------------------------------------
    //  Конструкторы
    // ---------------------------------------------------------------

    public Checkpoint() {
        this(0);
    }

    public Checkpoint(int order) {
        this.order = order;
        setupImage();
    }

    // ---------------------------------------------------------------
    //  Инициализация изображения
    // ---------------------------------------------------------------

    /**
     * Устанавливает прозрачное изображение-заглушку.
     * В DEBUG-режиме можно раскомментировать отображение номера.
     */
    private void setupImage() {
        GreenfootImage img = new GreenfootImage(TRIGGER_SIZE, TRIGGER_SIZE);

        // DEBUG: раскомментировать для визуализации чекпоинтов
        // img.setColor(new Color(255, 255, 0, 80));
        // img.fill();
        // img.setColor(Color.YELLOW);
        // img.setFont(new Font("Arial", true, false, 16));
        // img.drawString(String.valueOf(order), TRIGGER_SIZE / 2 - 5, TRIGGER_SIZE / 2 + 6);

        // Полностью прозрачное изображение (невидимый триггер)
        img.clear();
        setImage(img);
    }

    // ---------------------------------------------------------------
    //  Главный цикл
    // ---------------------------------------------------------------

    @Override
    public void act() {
        // Получаем мир и проверяем, что это RaceWorld
        greenfoot.World w = getWorld();
        if (!(w instanceof RaceWorld)) return;

        RaceWorld raceWorld = (RaceWorld) w;
        RaceManager manager = raceWorld.getRaceManager();
        if (manager == null) return;

        LapTimer timer = manager.getLapTimer();
        if (timer == null) return;

        // Ищем любую машину, пересекающую этот чекпоинт
        Car car = (Car) getOneIntersectingObject(Car.class);
        if (car != null) {
            timer.onCheckpointPassed(car, order);
        }
    }

    // ---------------------------------------------------------------
    //  Геттеры и сеттеры
    // ---------------------------------------------------------------

    /** Возвращает порядковый номер. Используется BotAI для сортировки. */
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
