import greenfoot.*;
import java.util.*;

/**
 * Игровой мир — гоночная трасса.
 * Разрешение: 1280x720.
 *
 * Получает из TrackSelectWorld:
 *   - gameMode    : режим игры
 *   - totalLaps   : 3 (Race/PvP) или 2 (Time: 1 прогр. + 1 зачётный)
 *   - trackIndex  : выбранная трасса (0/1/2)
 *   - player1Team : команда игрока 1
 *   - player2Team : команда игрока 2 (или -1)
 */
public class RaceWorld extends World {

    // ---------------------------------------------------------------
    //  Режимы игры
    // ---------------------------------------------------------------

    public static final int MODE_VS_BOTS    = 0;
    public static final int MODE_TIME_TRIAL = 1;
    public static final int MODE_TWO_PLAYER = 2;

    // ---------------------------------------------------------------
    //  Детектирование дороги по цвету пикселя
    // ---------------------------------------------------------------

    private static final int ROAD_MIN  = 30;
    private static final int ROAD_MAX  = 80;
    private static final int ROAD_DIFF = 25;

    // ---------------------------------------------------------------
    //  HUD
    // ---------------------------------------------------------------

    private static final int HUD_X     = 13;
    private static final int HUD_Y     = 13;
    private static final int HUD_LINE  = 19;

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final int gameMode;
    private final int trackIndex;
    private final int player1Team;
    private final int player2Team;

    private RaceManager raceManager;
    private boolean     raceFinished = false;

    private GreenfootImage trackImage;   // кеш для getColorAt

    // ---------------------------------------------------------------
    //  Конструкторы
    // ---------------------------------------------------------------

    /** Используется Greenfoot-редактором для быстрого запуска. */
    public RaceWorld() {
        this(MODE_VS_BOTS, 3, TrackSelectWorld.TRACK_IGORA,
             TeamSelectWorld.TEAM_FERRARI, -1);
    }

    public RaceWorld(int mode, int totalLaps, int trackIndex,
                     int player1Team, int player2Team) {
        super(1280, 720, 1);
        this.gameMode    = mode;
        this.trackIndex  = trackIndex;
        this.player1Team = player1Team;
        this.player2Team = player2Team;

        loadTrack();

        raceManager = new RaceManager(this, mode, totalLaps, player1Team, player2Team);
        raceManager.setupRace();
    }

    // ---------------------------------------------------------------
    //  Загрузка трассы
    // ---------------------------------------------------------------

    private void loadTrack() {
        String file = TrackSelectWorld.TRACK_FILES[trackIndex];
        try {
            trackImage = new GreenfootImage(file);
            // Масштабируем под 1280x720
            trackImage.scale(1280, 720);
            setBackground(new GreenfootImage(trackImage));
        } catch (Exception e) {
            trackImage = createFallbackTrack();
            setBackground(new GreenfootImage(trackImage));
        }
    }

    private GreenfootImage createFallbackTrack() {
        GreenfootImage img = new GreenfootImage(1280, 720);
        img.setColor(new Color(34, 139, 34));
        img.fill();
        img.setColor(new Color(60, 60, 60));
        img.fillOval(67, 67, 1147, 587);
        img.setColor(new Color(34, 139, 34));
        img.fillOval(200, 147, 880, 427);
        img.setColor(Color.WHITE);
        img.fillRect(587, 587, 107, 8);
        return img;
    }

    // ---------------------------------------------------------------
    //  Чекпоинты — координаты под каждую трассу
    // ---------------------------------------------------------------

    /**
     * Создаёт и добавляет чекпоинты для выбранной трассы.
     * Координаты подобраны под каждый трек при масштабе 1280x720.
     */
    public List<Checkpoint> setupCheckpoints() {
        int[][] positions;

        switch (trackIndex) {
            case TrackSelectWorld.TRACK_SILVERSTONE:
                positions = new int[][] {
                    {640, 633},   // 0 — старт (низ)
                    {1133, 467},  // 1 — правый низ
                    {1067, 133},  // 2 — правый верх
                    {640, 100},   // 3 — верх центр
                    {200, 200},   // 4 — левый верх
                    {147, 467},   // 5 — левый низ
                };
                break;
            case TrackSelectWorld.TRACK_SOCHI:
                positions = new int[][] {
                    {933, 600},   // 0 — старт
                    {1133, 400},  // 1
                    {933, 167},   // 2
                    {533, 167},   // 3
                    {267, 333},   // 4
                    {467, 600},   // 5
                };
                break;
            default: // IGORA
                positions = new int[][] {
                    {640, 600},   // 0 — старт
                    {1067, 400},  // 1
                    {933, 133},   // 2
                    {400, 133},   // 3
                    {200, 400},   // 4
                };
                break;
        }

        List<Checkpoint> list = new ArrayList<>();
        for (int i = 0; i < positions.length; i++) {
            Checkpoint cp = new Checkpoint(i);
            addObject(cp, positions[i][0], positions[i][1]);
            list.add(cp);
        }
        return list;
    }

    // ---------------------------------------------------------------
    //  Главный цикл
    // ---------------------------------------------------------------

    @Override
    public void act() {
        if (raceFinished) return;

        raceManager.update();
        checkOffRoad();
        drawHUD();

        if (raceManager.isRaceFinished()) {
            raceFinished = true;
        }
    }

    // ---------------------------------------------------------------
    //  Off-road детектирование
    // ---------------------------------------------------------------

    private void checkOffRoad() {
        @SuppressWarnings("unchecked")
        List<Car> cars = (List<Car>) getObjects(Car.class);
        if (cars == null) return;

        for (Car car : cars) {
            int x = Math.max(0, Math.min(car.getX(), trackImage.getWidth()  - 1));
            int y = Math.max(0, Math.min(car.getY(), trackImage.getHeight() - 1));
            Color pixel = trackImage.getColorAt(x, y);
            car.setOffRoad(!isRoadPixel(pixel));
        }
    }

    private boolean isRoadPixel(Color c) {
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        int maxDiff = Math.max(Math.abs(r-g), Math.max(Math.abs(r-b), Math.abs(g-b)));
        if (maxDiff > ROAD_DIFF) return false;
        int avg = (r + g + b) / 3;
        return avg >= ROAD_MIN && avg <= ROAD_MAX;
    }

    // ---------------------------------------------------------------
    //  HUD
    // ---------------------------------------------------------------

    private void drawHUD() {
        LapTimer timer = raceManager.getLapTimer();
        if (timer == null) return;

        List<PlayerCar> players = raceManager.getPlayers();
        if (players == null || players.isEmpty()) return;

        GreenfootImage bg = getBackground();

        // Фон HUD
        int hudH = players.size() * HUD_LINE * 5 + 13;
        GreenfootImage panel = new GreenfootImage(173, hudH);
        panel.setColor(new Color(0, 0, 0, 170));
        panel.fill();
        bg.drawImage(panel, HUD_X, HUD_Y);

        bg.setFont(new Font("Arial", true, false, 14));

        for (int i = 0; i < players.size(); i++) {
            PlayerCar p   = players.get(i);
            int yOff      = HUD_Y + 10 + i * HUD_LINE * 5;
            int lap       = timer.getCurrentLap(p);
            int totalLaps = timer.getTotalLaps();
            long best     = timer.getBestLapTime(p);
            long total    = timer.getTotalRaceTime(p);

            // Для Time Trial: прогревочный круг — особая метка
            String lapLabel;
            if (gameMode == MODE_TIME_TRIAL && lap == 1) {
                lapLabel = "ПРОГРЕВ";
            } else {
                int displayLap = (gameMode == MODE_TIME_TRIAL) ? lap - 1 : lap;
                int displayTotal = (gameMode == MODE_TIME_TRIAL) ? totalLaps - 1 : totalLaps;
                lapLabel = "Круг " + Math.min(displayLap, displayTotal) + "/" + displayTotal;
            }

            bg.setColor(Color.WHITE);
            bg.drawString("Игрок " + p.getPlayerNumber(), HUD_X + 8, yOff + HUD_LINE);
            bg.drawString(lapLabel,                       HUD_X + 8, yOff + HUD_LINE * 2);
            bg.drawString("Лучший: " + RaceResult.formatTime(best),  HUD_X + 8, yOff + HUD_LINE * 3);
            bg.drawString("Время:  " + RaceResult.formatTime(total), HUD_X + 8, yOff + HUD_LINE * 4);
        }

        setBackground(bg);
    }

    // ---------------------------------------------------------------
    //  Геттеры
    // ---------------------------------------------------------------

    public RaceManager getRaceManager() { return raceManager; }
    public int  getGameMode()           { return gameMode; }
    public boolean isRaceFinished()     { return raceFinished; }
    public int  getTrackIndex()         { return trackIndex; }
    public int  getPlayer1Team()        { return player1Team; }
    public int  getPlayer2Team()        { return player2Team; }
}
