import greenfoot.*;
import java.util.*;

/**
 * Учёт кругов, чекпоинтов и времени гонки.
 *
 * Логика:
 *   - Каждая машина имеет своё состояние (CarState).
 *   - Чекпоинты проходятся строго по порядку (антишорткат).
 *   - Круг засчитывается, когда пройден последний чекпоинт и машина
 *     снова пересекает чекпоинт с order == 0.
 *   - При достижении totalLaps машина помечается как финишировавшая.
 */
public class LapTimer {

    // ---------------------------------------------------------------
    //  Внутренний класс — состояние одной машины
    // ---------------------------------------------------------------

    private static class CarState {
        int  nextCheckpoint = 0;   // индекс следующего ожидаемого чекпоинта
        int  completedLaps  = 0;   // завершённых кругов
        boolean finished    = false;

        long lapStartTime   = 0;   // System.currentTimeMillis() на старте текущего круга
        long bestLapTime    = Long.MAX_VALUE; // лучшее время круга (мс)
        long raceStartTime  = 0;   // время старта гонки
        long finishTime     = 0;   // время финиша (мс от старта гонки)

        // Список времён каждого круга (мс)
        List<Long> lapTimes = new ArrayList<>();
    }

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final int totalLaps;           // число кругов до финиша
    private final int totalCheckpoints;    // число чекпоинтов на трассе

    /** Состояние каждой зарегистрированной машины. */
    private final Map<Car, CarState> states = new LinkedHashMap<>();

    private boolean running = false;       // запущен ли таймер
    private long globalStartTime = 0;      // System.currentTimeMillis() при старте

    // ---------------------------------------------------------------
    //  Конструктор
    // ---------------------------------------------------------------

    /**
     * @param totalLaps         количество кругов для завершения гонки
     * @param totalCheckpoints  количество чекпоинтов на трассе
     */
    public LapTimer(int totalLaps, int totalCheckpoints) {
        this.totalLaps        = totalLaps;
        this.totalCheckpoints = totalCheckpoints;
    }

    // ---------------------------------------------------------------
    //  Регистрация и старт
    // ---------------------------------------------------------------

    /**
     * Регистрирует машину для отслеживания.
     * Вызывать до start().
     */
    public void registerCar(Car car) {
        states.put(car, new CarState());
    }

    /**
     * Запускает таймер (вызывается при старте гонки).
     */
    public void start() {
        globalStartTime = System.currentTimeMillis();
        long now = globalStartTime;

        for (CarState state : states.values()) {
            state.raceStartTime = now;
            state.lapStartTime  = now;
        }

        running = true;
    }

    /**
     * Останавливает таймер (вызывается после финиша гонки).
     */
    public void stop() {
        running = false;
    }

    // ---------------------------------------------------------------
    //  Обработка прохождения чекпоинта
    // ---------------------------------------------------------------

    /**
     * Вызывается из Checkpoint.act() при пересечении с машиной.
     *
     * @param car   машина, прошедшая чекпоинт
     * @param order порядковый номер чекпоинта (0-based)
     */
    public void onCheckpointPassed(Car car, int order) {
        if (!running) return;

        CarState state = states.get(car);
        if (state == null || state.finished) return;

        // Антишорткат: принимаем только ожидаемый чекпоинт
        if (order != state.nextCheckpoint) return;

        long now = System.currentTimeMillis();

        // Переходим к следующему чекпоинту
        state.nextCheckpoint++;

        // Проверяем завершение круга:
        // Круг завершён, когда пройдены ВСЕ чекпоинты (nextCheckpoint == totalCheckpoints)
        if (state.nextCheckpoint >= totalCheckpoints) {
            state.nextCheckpoint = 0; // готов к следующему кругу

            long lapTime = now - state.lapStartTime;
            state.lapTimes.add(lapTime);

            if (lapTime < state.bestLapTime) {
                state.bestLapTime = lapTime;
            }

            state.lapStartTime = now;
            state.completedLaps++;

            // Проверяем финиш
            if (state.completedLaps >= totalLaps) {
                state.finished    = true;
                state.finishTime  = now - state.raceStartTime;
            }
        }

        // Обновляем поле в самой машине (для отображения в HUD)
        car.setCurrentLap(state.completedLaps + 1);
        car.setCheckpointsPassed(
            state.completedLaps * totalCheckpoints + state.nextCheckpoint
        );
        car.setRaceTimeMs(now - state.raceStartTime);
    }

    // ---------------------------------------------------------------
    //  Обновление времени (вызывать каждый кадр)
    // ---------------------------------------------------------------

    /**
     * Обновляет raceTimeMs для всех незавершивших машин.
     * Вызывать из RaceWorld.act() каждый кадр.
     */
    public void update() {
        if (!running) return;
        long now = System.currentTimeMillis();
        for (Map.Entry<Car, CarState> entry : states.entrySet()) {
            CarState state = entry.getValue();
            if (!state.finished) {
                entry.getKey().setRaceTimeMs(now - state.raceStartTime);
                entry.getKey().setCurrentLap(state.completedLaps + 1);
            }
        }
    }

    // ---------------------------------------------------------------
    //  Публичные геттеры
    // ---------------------------------------------------------------

    /**
     * Возвращает текущий завершённый круг машины (1-based для HUD).
     * Например: 0 завершённых кругов → отображать "Круг 1".
     */
    public int getCurrentLap(Car car) {
        CarState state = states.get(car);
        return (state == null) ? 1 : state.completedLaps + 1;
    }

    /**
     * Возвращает лучшее время круга машины в мс.
     * Если круг ещё не был завершён — возвращает -1.
     */
    public long getBestLapTime(Car car) {
        CarState state = states.get(car);
        if (state == null || state.bestLapTime == Long.MAX_VALUE) return -1;
        return state.bestLapTime;
    }

    /**
     * Возвращает суммарное время гонки машины в мс.
     * Если машина финишировала — время на момент финиша.
     */
    public long getTotalRaceTime(Car car) {
        CarState state = states.get(car);
        if (state == null) return 0;
        if (state.finished)  return state.finishTime;
        return System.currentTimeMillis() - state.raceStartTime;
    }

    /**
     * Возвращает true, если машина финишировала.
     */
    public boolean isFinished(Car car) {
        CarState state = states.get(car);
        return (state != null) && state.finished;
    }

    /**
     * Возвращает список всех зарегистрированных машин.
     */
    public Set<Car> getCars() {
        return Collections.unmodifiableSet(states.keySet());
    }

    /** Число кругов для завершения гонки. */
    public int getTotalLaps() { return totalLaps; }

    /** Число чекпоинтов на трассе. */
    public int getTotalCheckpoints() { return totalCheckpoints; }
}
