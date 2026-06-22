import greenfoot.*;
import java.util.List;

/**
 * Искусственный интеллект бота.
 *
 * Принцип работы:
 *   1. На трассе расставлены объекты Checkpoint (напарник Б расставляет их в RaceWorld).
 *   2. BotAI знает список чекпоинтов в порядке прохождения.
 *   3. Каждый кадр AI вычисляет угол до следующего чекпоинта и рулит к нему.
 *   4. При достижении чекпоинта — переходит к следующему.
 *
 * Сложность влияет на:
 *   - угловой порог подруливания (EASY рулит грубее)
 *   - частоту ошибок (EASY иногда "промахивается")
 *   - дистанцию торможения перед поворотами
 */
public class BotAI {

    // --- Настройки по сложности ---
    // EASY: большой порог (грубый руль), часто ошибается
    // MEDIUM: средний порог, редкие ошибки
    // HARD: точный руль, без ошибок
    private static final double[] STEER_THRESHOLD = { 20.0, 10.0, 4.0 };   // градусы
    private static final double[] ERROR_CHANCE     = { 0.008, 0.002, 0.0 }; // вероятность/кадр
    private static final double[] BRAKE_ANGLE      = { 35.0, 25.0, 18.0 }; // угол торможения

    // Дистанция "достижения" чекпоинта (пиксели) — увеличена чтобы бот не промахивался
    private static final int REACH_DISTANCE = 80;

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final BotCar car;
    private final int    difficulty;

    private List<Checkpoint> waypoints; // заполняется при первом update()
    private int currentWaypointIndex = 0;
    private boolean errorActive = false; // если true — кадр ошибки (рулим не туда)

    // ---------------------------------------------------------------
    //  Конструктор
    // ---------------------------------------------------------------

    public BotAI(BotCar car, int difficulty) {
        this.car        = car;
        this.difficulty = difficulty;
    }

    // ---------------------------------------------------------------
    //  Главный метод — вызывается из BotCar.act()
    // ---------------------------------------------------------------

    public void update() {
        if (car.getWorld() == null) return;

        // Ленивая инициализация вейпоинтов
        if (waypoints == null) {
            loadWaypoints();
        }

        if (waypoints == null || waypoints.isEmpty()) {
            // Нет чекпоинтов — едем прямо
            car.accelerate();
            return;
        }

        Checkpoint target = waypoints.get(currentWaypointIndex);

        // Проверяем достижение текущего чекпоинта — переходим, но НЕ выходим из метода
        if (distanceTo(target) < REACH_DISTANCE) {
            currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size();
            errorActive = false;
            // Берём новую цель и продолжаем движение в этом же кадре
            target = waypoints.get(currentWaypointIndex);
        }

        // Случайная ошибка (только EASY/MEDIUM)
        if (Math.random() < ERROR_CHANCE[difficulty]) {
            errorActive = true;
        } else {
            errorActive = false;
        }

        // Угол до цели
        double angleToTarget = angleTo(target);
        double carAngle      = car.getCarAngle();
        double diff          = normalizeAngle(angleToTarget - carAngle);

        if (errorActive) {
            diff = -diff;
        }

        // Стратегия руля
        double threshold = STEER_THRESHOLD[difficulty];

        if (diff > threshold) {
            car.turnRight();
        } else if (diff < -threshold) {
            car.turnLeft();
        }

        // Газ или притормозить перед крутым поворотом.
        // Важно: не используем brake() (он даёт задний ход и блокирует поворот),
        // вместо этого просто не даём газ — трение само замедлит бота.
        if (Math.abs(diff) > BRAKE_ANGLE[difficulty]) {
            // Не газуем, но и не тормозим — трение замедлит
            // (brake() приводил к speed<0 и потере управления)
        } else {
            car.accelerate();
        }
    }

    // ---------------------------------------------------------------
    //  Вспомогательные методы
    // ---------------------------------------------------------------

    /**
     * Загружает все чекпоинты из мира.
     * Важно: Checkpoint должны быть добавлены в мир к этому моменту.
     * Список уже отсортирован по порядку прохождения (поле order в Checkpoint).
     */
    @SuppressWarnings("unchecked")
    private void loadWaypoints() {
        List<Checkpoint> all = (List<Checkpoint>) car.getWorld().getObjects(Checkpoint.class);
        if (all == null || all.isEmpty()) return;

        // Сортируем по полю order (напарник выставляет порядок при расстановке)
        all.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
        waypoints = all;
    }

    /**
     * Вычисляет расстояние от машины до актора.
     */
    private double distanceTo(Actor target) {
        int dx = target.getX() - car.getX();
        int dy = target.getY() - car.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Вычисляет угол (в градусах) от машины до цели.
     * 0 = вверх (север), 90 = вправо, как в Greenfoot.
     */
    private double angleTo(Actor target) {
        int dx = target.getX() - car.getX();
        int dy = target.getY() - car.getY();
        return Math.toDegrees(Math.atan2(dy, dx)) + 90;
    }

    /**
     * Нормализует угол в диапазон (-180, 180).
     */
    private double normalizeAngle(double a) {
        while (a >  180) a -= 360;
        while (a < -180) a += 360;
        return a;
    }

    // ---------------------------------------------------------------
    //  Публичный API для сброса (при новом заезде)
    // ---------------------------------------------------------------

    /**
     * Сбрасывает AI в начало трассы.
     * Вызывать при старте новой гонки.
     */
    public void reset() {
        currentWaypointIndex = 0;
        errorActive          = false;
        waypoints            = null; // перезагрузит при следующем update()
    }
}
