import greenfoot.*;
import java.util.*;

/**
 * Управляет созданием участников, режимами гонки и определением финиша.
 */
public class RaceManager {

    // ---------------------------------------------------------------
    //  Стартовые позиции по линии старта каждой трассы
    // ---------------------------------------------------------------

    private static final int[][] START_LINE = {
        {610, 145},  // 0 — Igora
        {888, 610},  // 1 — Silverstone
        {980, 370},  // 2 — Sochi
    };

    /**
     * Угол старта машин для каждой трассы (в градусах).
     * 0   = смотрит вверх
     * 90  = смотрит вправо
     * 180 = смотрит вниз
     * 270 = смотрит влево
     * Поставь нужные значения вместо нулей.
     */
    private static final int[] START_ANGLE = {
        -243,  // Igora       — TODO: поставь нужный угол
        -64,  // Silverstone — TODO: поставь нужный угол
        240,  // Sochi       — TODO: поставь нужный угол
    };

    /**
     * Смещения вокруг линии старта для трёх машин (x-offset, y-offset).
     */
    private static final int[][] GRID_OFFSETS = {
        {0,   0},   // слот 0 (игрок)
        {40, 0},  // слот 1
        {0,  40},  // слот 2
    };

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final RaceWorld world;
    private final int       gameMode;
    private final int       totalLaps;
    private final int       trackIndex;
    private final int       player1Team;
    private final int       player2Team;

    private LapTimer lapTimer;

    private final List<PlayerCar> players = new ArrayList<>();
    private final List<BotCar>    bots    = new ArrayList<>();

    private final List<Car> finishOrder = new ArrayList<>();
    private boolean raceFinished = false;

    // ---------------------------------------------------------------
    //  Конструктор
    // ---------------------------------------------------------------

    public RaceManager(RaceWorld world, int gameMode, int totalLaps, int trackIndex,
                       int player1Team, int player2Team) {
        this.world       = world;
        this.gameMode    = gameMode;
        this.totalLaps   = totalLaps;
        this.trackIndex  = trackIndex;
        this.player1Team = player1Team;
        this.player2Team = player2Team;
    }

    // ---------------------------------------------------------------
    //  Настройка гонки
    // ---------------------------------------------------------------

    public void setupRace() {
        List<Checkpoint> checkpoints = world.setupCheckpoints();
        lapTimer = new LapTimer(totalLaps, checkpoints.size());

        switch (gameMode) {
            case RaceWorld.MODE_VS_BOTS:    setupVsBots();    break;
            case RaceWorld.MODE_TIME_TRIAL: setupTimeTrial(); break;
            case RaceWorld.MODE_TWO_PLAYER: setupTwoPlayer(); break;
            default: setupVsBots();
        }

        for (PlayerCar p : players) lapTimer.registerCar(p);
        for (BotCar    b : bots)    lapTimer.registerCar(b);

        lapTimer.start();
    }

    // ---------------------------------------------------------------
    //  Вспомогательный метод: спавн машины с нужным углом
    // ---------------------------------------------------------------

    private void spawnCar(Car car, int slot) {
        int lineX = START_LINE[trackIndex][0];
        int lineY = START_LINE[trackIndex][1];
        int x = lineX + GRID_OFFSETS[slot][0];
        int y = lineY + GRID_OFFSETS[slot][1];
        world.addObject(car, x, y);
        // Устанавливаем начальный угол поворота
        car.setStartAngle(START_ANGLE[trackIndex]);
    }

    // ---------------------------------------------------------------
    //  Создание участников
    // ---------------------------------------------------------------

    private void setupVsBots() {
        PlayerCar player = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1,
                                         TeamSelectWorld.TEAM_SPRITES[player1Team]);
        spawnCar(player, 0);
        players.add(player);

        List<Integer> botTeams = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (i != player1Team) botTeams.add(i);
        }

        for (int i = 0; i < botTeams.size(); i++) {
            String sprite = TeamSelectWorld.TEAM_SPRITES[botTeams.get(i)];
            BotCar bot    = new BotCar(BotCar.MEDIUM, i, sprite);
            spawnCar(bot, i + 1);
            bots.add(bot);
        }
    }

    private void setupTimeTrial() {
        PlayerCar player = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1,
                                         TeamSelectWorld.TEAM_SPRITES[player1Team]);
        spawnCar(player, 0);
        players.add(player);
    }

    private void setupTwoPlayer() {
        PlayerCar p1 = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1,
                                     TeamSelectWorld.TEAM_SPRITES[player1Team]);
        PlayerCar p2 = new PlayerCar(PlayerCar.SCHEME_WASD, 2,
                                     TeamSelectWorld.TEAM_SPRITES[player2Team]);
        spawnCar(p1, 0);
        spawnCar(p2, 1);
        players.add(p1);
        players.add(p2);
    }

    // ---------------------------------------------------------------
    //  Обновление (каждый кадр)
    // ---------------------------------------------------------------

    public void update() {
        if (raceFinished) return;

        lapTimer.update();

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
    //  Финиш
    // ---------------------------------------------------------------

    private void onCarFinished(Car car) {
        finishOrder.add(car);
        car.setPosition(finishOrder.size());

        boolean shouldEnd = false;

        switch (gameMode) {
            case RaceWorld.MODE_VS_BOTS:
            case RaceWorld.MODE_TIME_TRIAL:
                for (PlayerCar p : players) {
                    if (lapTimer.isFinished(p)) { shouldEnd = true; break; }
                }
                break;
            case RaceWorld.MODE_TWO_PLAYER:
                shouldEnd = players.stream().allMatch(lapTimer::isFinished);
                break;
        }

        if (shouldEnd) endRace();
    }

    private void endRace() {
        if (raceFinished) return;
        raceFinished = true;
        lapTimer.stop();

        List<Car> allCars = new ArrayList<>();
        allCars.addAll(players);
        allCars.addAll(bots);

        List<Car> unfinished = new ArrayList<>();
        for (Car car : allCars) {
            if (!finishOrder.contains(car)) unfinished.add(car);
        }
        unfinished.sort((a, b) ->
            Integer.compare(b.getCheckpointsPassed(), a.getCheckpointsPassed()));
        for (Car car : unfinished) {
            finishOrder.add(car);
            car.setPosition(finishOrder.size());
        }

        List<RaceResult> results = buildResults();
        Greenfoot.setWorld(new ResultWorld(results, gameMode));
    }

    // ---------------------------------------------------------------
    //  Результаты
    // ---------------------------------------------------------------

    private List<RaceResult> buildResults() {
        List<RaceResult> results = new ArrayList<>();
        for (int i = 0; i < finishOrder.size(); i++) {
            Car    car  = finishOrder.get(i);
            String name = getCarName(car);
            long   time = lapTimer.getTotalRaceTime(car);
            long   best = lapTimer.getBestLapTime(car);
            results.add(new RaceResult(i + 1, name, time, best));
        }
        return results;
    }

    private String getCarName(Car car) {
        if (car instanceof PlayerCar) {
            return "Игрок " + ((PlayerCar) car).getPlayerNumber();
        } else if (car instanceof BotCar) {
            return "Бот";
        }
        return "Участник";
    }

    // ---------------------------------------------------------------
    //  Геттеры
    // ---------------------------------------------------------------

    public LapTimer         getLapTimer()    { return lapTimer; }
    public boolean          isRaceFinished() { return raceFinished; }
    public List<PlayerCar>  getPlayers()     { return Collections.unmodifiableList(players); }
    public List<BotCar>     getBots()        { return Collections.unmodifiableList(bots); }
    public List<Car>        getFinishOrder() { return Collections.unmodifiableList(finishOrder); }
}
