import greenfoot.*;
import java.util.*;

/**
 * Управляет созданием участников, режимами гонки и определением финиша.
 *
 * Изменения:
 *   - Принимает player1Team / player2Team из TeamSelectWorld.
 *   - Race: 1 игрок + 2 бота (команды которые игрок не взял).
 *   - Time Trial: 1 игрок, без ботов, круг 1 = прогревочный (не засчитывается).
 *   - PvP: 2 игрока, без ботов.
 *   - Спрайты машин берутся из TeamSelectWorld.TEAM_SPRITES.
 */
public class RaceManager {

    // ---------------------------------------------------------------
    //  Стартовые позиции (3 слота — для трёх машин в Race)
    // ---------------------------------------------------------------

    private static final int START_X_1 = 930;   // левая колонка
    private static final int START_X_2 = 990;   // правая колонка

    private static final int START_Y_1 = 940;   // первый ряд (ближе к финишной черте)
    private static final int START_Y_2 = 980;   // второй ряд

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final RaceWorld world;
    private final int       gameMode;
    private final int       totalLaps;
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

    public RaceManager(RaceWorld world, int gameMode, int totalLaps,
                       int player1Team, int player2Team) {
        this.world       = world;
        this.gameMode    = gameMode;
        this.totalLaps   = totalLaps;
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
    //  Создание участников
    // ---------------------------------------------------------------

    private void setupVsBots() {
        // Игрок 1
        PlayerCar player = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1,
                                         TeamSelectWorld.TEAM_SPRITES[player1Team]);
        world.addObject(player, START_X_1, START_Y_1);
        players.add(player);

        // 2 бота — команды которые игрок не взял
        List<Integer> botTeams = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (i != player1Team) botTeams.add(i);
        }

        int[] startX = { START_X_2, START_X_1 };
        int[] startY = { START_Y_1, START_Y_2 };

        for (int i = 0; i < botTeams.size(); i++) {
            String sprite = TeamSelectWorld.TEAM_SPRITES[botTeams.get(i)];
            BotCar bot    = new BotCar(BotCar.MEDIUM, i, sprite);
            world.addObject(bot, startX[i], startY[i]);
            bots.add(bot);
        }
    }

    private void setupTimeTrial() {
        // Только игрок, без ботов
        PlayerCar player = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1,
                                         TeamSelectWorld.TEAM_SPRITES[player1Team]);
        world.addObject(player, START_X_1, START_Y_1);
        players.add(player);
    }

    private void setupTwoPlayer() {
        PlayerCar p1 = new PlayerCar(PlayerCar.SCHEME_ARROWS, 1,
                                     TeamSelectWorld.TEAM_SPRITES[player1Team]);
        PlayerCar p2 = new PlayerCar(PlayerCar.SCHEME_WASD, 2,
                                     TeamSelectWorld.TEAM_SPRITES[player2Team]);
        world.addObject(p1, START_X_1, START_Y_1);
        world.addObject(p2, START_X_2, START_Y_1);
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
                // Завершаем когда игрок финишировал
                for (PlayerCar p : players) {
                    if (lapTimer.isFinished(p)) { shouldEnd = true; break; }
                }
                break;
            case RaceWorld.MODE_TWO_PLAYER:
                // Завершаем когда ОБА игрока финишировали
                shouldEnd = players.stream().allMatch(lapTimer::isFinished);
                break;
        }

        if (shouldEnd) endRace();
    }

    private void endRace() {
        if (raceFinished) return;
        raceFinished = true;
        lapTimer.stop();

        // Назначаем позиции незавершившим
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
