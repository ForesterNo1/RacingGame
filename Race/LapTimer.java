import greenfoot.*;
import java.util.*;

/**
 * Учёт кругов, чекпоинтов и времени гонки.
 *
 * Прогревочный круг (Time Trial):
 *   Круг 1 — прогревочный: машина едет нормально, но время не записывается
 *   как зачётное. Засчитывается только круг 2 (totalLaps = 2, зачётный = 1).
 *   Флаг warmupDone хранится в CarState.
 */
public class LapTimer {

    // ---------------------------------------------------------------
    //  Внутренний класс
    // ---------------------------------------------------------------

    private static class CarState {
        int  nextCheckpoint = 0;
        int  completedLaps  = 0;
        boolean finished    = false;
        boolean warmupDone  = false;  // для Time Trial

        long lapStartTime  = 0;
        long bestLapTime   = Long.MAX_VALUE;
        long raceStartTime = 0;
        long finishTime    = 0;

        List<Long> lapTimes = new ArrayList<>();
    }

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final int totalLaps;
    private final int totalCheckpoints;

    /**
     * Если true — первый круг является прогревочным и не записывается
     * как зачётное время. Устанавливается из RaceManager для Time Trial.
     */
    private boolean warmupLapMode = false;

    private final Map<Car, CarState> states = new LinkedHashMap<>();
    private boolean running = false;
    private long globalStartTime = 0;

    // ---------------------------------------------------------------
    //  Конструктор
    // ---------------------------------------------------------------

    public LapTimer(int totalLaps, int totalCheckpoints) {
        this.totalLaps        = totalLaps;
        this.totalCheckpoints = totalCheckpoints;
    }

    /** Включает режим прогревочного круга (Time Trial). */
    public void setWarmupLapMode(boolean enabled) {
        this.warmupLapMode = enabled;
    }

    // ---------------------------------------------------------------
    //  Регистрация и старт
    // ---------------------------------------------------------------

    public void registerCar(Car car) {
        states.put(car, new CarState());
    }

    public void start() {
        globalStartTime = System.currentTimeMillis();
        for (CarState state : states.values()) {
            state.raceStartTime = globalStartTime;
            state.lapStartTime  = globalStartTime;
        }
        running = true;
    }

    public void stop() {
        running = false;
    }

    // ---------------------------------------------------------------
    //  Прохождение чекпоинта
    // ---------------------------------------------------------------

    public void onCheckpointPassed(Car car, int order) {
        if (!running) return;

        CarState state = states.get(car);
        if (state == null || state.finished) return;
        if (order != state.nextCheckpoint) return;

        long now = System.currentTimeMillis();
        state.nextCheckpoint++;

        if (state.nextCheckpoint >= totalCheckpoints) {
            state.nextCheckpoint = 0;

            long lapTime = now - state.lapStartTime;
            state.lapStartTime = now;
            state.completedLaps++;

            // Прогревочный круг — не записываем время
            boolean isWarmup = warmupLapMode && !state.warmupDone;
            if (isWarmup) {
                state.warmupDone = true;
                // Не добавляем в lapTimes, не обновляем bestLapTime
            } else {
                state.lapTimes.add(lapTime);
                if (lapTime < state.bestLapTime) {
                    state.bestLapTime = lapTime;
                }
            }

            // Финиш: для Time Trial нужно completedLaps == totalLaps
            if (state.completedLaps >= totalLaps) {
                state.finished   = true;
                state.finishTime = now - state.raceStartTime;
            }
        }

        car.setCurrentLap(state.completedLaps + 1);
        car.setCheckpointsPassed(
            state.completedLaps * totalCheckpoints + state.nextCheckpoint);
        car.setRaceTimeMs(now - state.raceStartTime);
    }

    // ---------------------------------------------------------------
    //  Обновление каждый кадр
    // ---------------------------------------------------------------

    public void update() {
        if (!running) return;
        long now = System.currentTimeMillis();
        for (Map.Entry<Car, CarState> e : states.entrySet()) {
            CarState state = e.getValue();
            if (!state.finished) {
                e.getKey().setRaceTimeMs(now - state.raceStartTime);
                e.getKey().setCurrentLap(state.completedLaps + 1);
            }
        }
    }

    // ---------------------------------------------------------------
    //  Геттеры
    // ---------------------------------------------------------------

    public int  getCurrentLap(Car car) {
        CarState s = states.get(car);
        return (s == null) ? 1 : s.completedLaps + 1;
    }

    public long getBestLapTime(Car car) {
        CarState s = states.get(car);
        if (s == null || s.bestLapTime == Long.MAX_VALUE) return -1;
        return s.bestLapTime;
    }

    public long getTotalRaceTime(Car car) {
        CarState s = states.get(car);
        if (s == null) return 0;
        if (s.finished) return s.finishTime;
        return System.currentTimeMillis() - s.raceStartTime;
    }

    public boolean isFinished(Car car) {
        CarState s = states.get(car);
        return (s != null) && s.finished;
    }

    public boolean isWarmupLap(Car car) {
        CarState s = states.get(car);
        return warmupLapMode && s != null && !s.warmupDone;
    }

    public Set<Car>  getCars()             { return Collections.unmodifiableSet(states.keySet()); }
    public int       getTotalLaps()        { return totalLaps; }
    public int       getTotalCheckpoints() { return totalCheckpoints; }
}
