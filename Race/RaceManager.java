import greenfoot.*;
import java.util.*;

/**
 * Управляет созданием участников, режимами гонки и определением финиша.
 *
 * Режимы:
 *   MODE_VS_BOTS    — 1 игрок vs 5 ботов; гонка завершается, когда игрок финишировал.
 *   MODE_TIME_TRIAL — 1 игрок без ботов; гонка завершается после финиша игрока.
 *   MODE_TWO_PLAYER — 2 игрока; гонка завершается, когда ОБА финишировали.
 */
public class RaceManager {

    // ---------------------------------------------------------------
    //  Стартовые позиции (6 слотов — для двух рядов)
    // ---------------------------------------------------------------

    /** X-смещения для 2 колонок на старте. */
    private static final int[] START_COL_X = { 355, 395 };

    /** Y-позиции для 3 строк на старте (ближе к нижней финишной черте). */
    private static final int[] START_ROW_Y = { 520, 545, 570 };

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final RaceWorld world;
    private final int       gameMode;
    private final int       totalLaps;

    private LapTimer lapTimer;

    private final List<PlayerCar> players = new ArrayList<>();
    private final List<BotCar>    bots    = new ArrayList<>();

    /** Список финишировавших в порядке финиша. */
    private final List<Car> finishOrder = new ArrayList<>();

    private boolean raceFinished = false;

    // ---------------------------------------------------------------
    //  Конструктор
    // ---------------------------------------------------------------

    /**
     * @param world     текущий игровой мир
     * @param gameMode  одна из констант RaceWorld.MODE_*
     * @param totalLaps количество кругов в гонке
     */
    public RaceManager(RaceWorld world, int gameMode, int totalLaps) {
        this.world     = world;
        this.gameMode  = gameMode;
        this.totalLaps = totalLaps;
    }

    /** Конструктор по умолчанию (1 круг). */
    public RaceManager(RaceWorld world, int gameMode) {
        this(world, gameMode, 3);
    }

    // ---------------------------------------------------------------
    //  Настройка гонки
    // ---------------------------------------------------------------

    /**
     * Главный метод инициализации.
     * Вызывать из конструктора RaceWorld после создания RaceManager.
     */
    public void setupRace() {
        // 1. Создаём чекпоинты через мир (мир их расставляет и возвращает список)
        List<Checkpoint> checkpoints = world.setupCheckpoints();

        // 2. Создаём LapTimer
        lapTimer = new LapTimer(totalLaps, checkpoints.size());

        // 3. Создаём машины в зависимости от режима
        switch (gameMode) {
            case RaceWorld.MODE_VS_BOTS:
                setupVsBots();
                break;
            case RaceWorld.MODE_TIME_TRIAL:
                setupTimeTrial();
                break;
            case RaceWorld.MODE_TWO_PLAYER:
                setupTwoPlayer();
                break;
            default:
                setupVsBots();
        }

        // 4. Регистрируем все машины в LapTimer
        for (PlayerCar p : players) lapTimer.registerCar(p);
        for (BotCar    b : bots)    lapTimer.registerCar(b);

        // 5. Запускаем таймер
        lapTimer.start();
    }

    // ---------------------------------------------------------------
    //  Вспомогательные методы создания участников
    // ---------------------------------------------------------------

    private void setupVsBots() {
        // 1 игрок + 5 ботов (EASY, EASY, MEDIUM, MEDIUM, HARD)
        PlayerCar player = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1);
        world.addObject(player, START_COL_X[0], START_ROW_Y[0]);
        players.add(player);

        int[] difficulties = {
            BotCar.EASY, BotCar.EASY,
            BotCar.MEDIUM, BotCar.MEDIUM,
            BotCar.HARD
        };

        for (int i = 0; i < 5; i++) {
            BotCar bot = new BotCar(difficulties[i], i);
            int col = i % 2;
            int row = (i / 2) + 1; // ряды 1..2 (игрок в ряду 0)
            if (row >= START_ROW_Y.length) row = START_ROW_Y.length - 1;
            world.addObject(bot, START_COL_X[col], START_ROW_Y[row]);
            bots.add(bot);
        }
    }

    private void setupTimeTrial() {
        // Только 1 игрок, без ботов
        PlayerCar player = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1);
        world.addObject(player, START_COL_X[0], START_ROW_Y[0]);
        players.add(player);
    }

    private void setupTwoPlayer() {
        // 2 игрока, без ботов
        PlayerCar p1 = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1);
        PlayerCar p2 = new PlayerCar(PlayerCar.SCHEME_WASD,   2);
        world.addObject(p1, START_COL_X[0], START_ROW_Y[0]);
        world.addObject(p2, START_COL_X[1], START_ROW_Y[0]);
        players.add(p1);
        players.add(p2);
    }

    // ---------------------------------------------------------------
    //  Обновление (вызывать каждый кадр из RaceWorld.act())
    // ---------------------------------------------------------------

    /**
     * Обновляет LapTimer и проверяет финиш каждой машины.
     */
    public void update() {
        if (raceFinished) return;

        lapTimer.update();

        // Проверяем финиш каждой машины
        List<Car> allCars = new ArrayList<>();
        allCars.addAll(players);
        allCars.addAll(bots);

        for (Car car : allCars) {
            if (!finishOrder.contains(car) && lapTimer.isFinished(car)) {
                onCarFinished(car);
            }
        }
    }

    // ---------------------------------------------------------------
    //  Обработка финиша машины
    // ---------------------------------------------------------------

    /**
     * Вызывается, когда машина финишировала.
     * Назначает позицию и проверяет условие завершения гонки.
     */
    private void onCarFinished(Car car) {
        finishOrder.add(car);
        car.setPosition(finishOrder.size()); // 1-е место, 2-е место, ...

        // Проверяем условие завершения гонки в зависимости от режима
        boolean shouldEnd = false;

        switch (gameMode) {
            case RaceWorld.MODE_VS_BOTS:
            case RaceWorld.MODE_TIME_TRIAL:
                // Завершаем, когда ИГРОК финишировал
                for (PlayerCar p : players) {
                    if (lapTimer.isFinished(p)) {
                        shouldEnd = true;
                        break;
                    }
                }
                break;

            case RaceWorld.MODE_TWO_PLAYER:
                // Завершаем, когда ОБА игрока финишировали
                shouldEnd = players.stream().allMatch(lapTimer::isFinished);
                break;
        }

        if (shouldEnd) {
            endRace();
        }
    }

    /**
     * Завершает гонку: назначает позиции незавершившим машинам,
     * останавливает таймер и переключает мир на ResultWorld.
     */
    private void endRace() {
        if (raceFinished) return;
        raceFinished = true;
        lapTimer.stop();

        // Назначаем позиции всем, кто ещё не финишировал (сортировка по прогрессу)
        List<Car> unfinished = new ArrayList<>();
        List<Car> allCars    = new ArrayList<>();
        allCars.addAll(players);
        allCars.addAll(bots);

        for (Car car : allCars) {
            if (!finishOrder.contains(car)) {
                unfinished.add(car);
            }
        }

        // Сортируем незавершивших по кол-ву пройденных чекпоинтов (больше = лучше)
        unfinished.sort((a, b) ->
            Integer.compare(b.getCheckpointsPassed(), a.getCheckpointsPassed())
        );

        for (Car car : unfinished) {
            finishOrder.add(car);
            car.setPosition(finishOrder.size());
        }

        // Формируем список результатов и переходим в ResultWorld
        List<RaceResult> results = buildResults();
        Greenfoot.setWorld(new ResultWorld(results, gameMode));
    }

    // ---------------------------------------------------------------
    //  Формирование результатов
    // ---------------------------------------------------------------

    /**
     * Создаёт список RaceResult для ResultWorld.
     */
    private List<RaceResult> buildResults() {
        List<RaceResult> results = new ArrayList<>();

        for (int i = 0; i < finishOrder.size(); i++) {
            Car car = finishOrder.get(i);
            String name = getCarName(car);
            long   time = lapTimer.getTotalRaceTime(car);
            long   best = lapTimer.getBestLapTime(car);
            results.add(new RaceResult(i + 1, name, time, best));
        }

        return results;
    }

    /**
     * Возвращает отображаемое имя машины.
     */
    private String getCarName(Car car) {
        if (car instanceof PlayerCar) {
            return "Игрок " + ((PlayerCar) car).getPlayerNumber();
        } else if (car instanceof BotCar) {
            int diff = ((BotCar) car).getDifficulty();
            String[] levels = {"Лёгкий", "Средний", "Сложный"};
            String level = (diff >= 0 && diff < levels.length) ? levels[diff] : "?";
            return "Бот (" + level + ")";
        }
        return "Участник";
    }

    // ---------------------------------------------------------------
    //  Геттеры
    // ---------------------------------------------------------------

    public LapTimer    getLapTimer()     { return lapTimer; }
    public boolean     isRaceFinished()  { return raceFinished; }
    public List<Car>   getFinishOrder()  { return Collections.unmodifiableList(finishOrder); }
    public List<PlayerCar> getPlayers()  { return Collections.unmodifiableList(players); }
    public List<BotCar>    getBots()     { return Collections.unmodifiableList(bots); }
}
