import greenfoot.*;
import java.util.*;

/**
 * Игровой мир — гоночная трасса.
 *
 * Реализация разработчика Б:
 *   - Загрузка фона трассы.
 *   - Расстановка чекпоинтов (setupCheckpoints).
 *   - Создание RaceManager, который сам создаёт машины и LapTimer.
 *   - Проверка offRoad по цвету пикселя фона.
 *   - Отображение простого HUD (круг, лучшее время).
 */
public class RaceWorld extends World {

    // ---------------------------------------------------------------
    //  Константы режимов
    // ---------------------------------------------------------------

    public static final int MODE_VS_BOTS    = 0;
    public static final int MODE_TIME_TRIAL = 1;
    public static final int MODE_TWO_PLAYER = 2;

    // ---------------------------------------------------------------
    //  Детектирование дороги по цвету пикселя
    // ---------------------------------------------------------------

    /**
     * Порог для определения "серого" пикселя дороги.
     * Пиксель считается дорогой, если все каналы (R, G, B) находятся
     * в диапазоне [ROAD_MIN, ROAD_MAX] и разница между каналами < ROAD_DIFF.
     * Настройте под конкретную текстуру трассы.
     */
    private static final int ROAD_MIN  = 60;
    private static final int ROAD_MAX  = 200;
    private static final int ROAD_DIFF = 40; // максимальная разница R-G-B (серость)

    // ---------------------------------------------------------------
    //  HUD — параметры отображения
    // ---------------------------------------------------------------

    private static final int HUD_X      = 10;
    private static final int HUD_Y      = 10;
    private static final int HUD_WIDTH  = 200;
    private static final int HUD_LINE_H = 20;

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final int         gameMode;
    private       RaceManager raceManager;
    private       boolean     raceFinished = false;

    /** Кешированное изображение фона для getColorAt(). */
    private GreenfootImage trackImage;

    // ---------------------------------------------------------------
    //  Конструкторы
    // ---------------------------------------------------------------

    public RaceWorld() {
        this(MODE_VS_BOTS, 3);
    }

    public RaceWorld(int mode) {
        this(mode, 3);
    }

    public RaceWorld(int mode, int totalLaps) {
        super(800, 600, 1);
        this.gameMode = mode;

        // Загружаем фоновое изображение трассы
        loadTrack();

        // Создаём менеджер гонки и запускаем всё
        raceManager = new RaceManager(this, mode, totalLaps);
        raceManager.setupRace();
    }

    // ---------------------------------------------------------------
    //  Загрузка трассы
    // ---------------------------------------------------------------

    private void loadTrack() {
        try {
            trackImage = new GreenfootImage("track_1.png");
            setBackground(new GreenfootImage("track_1.png")); // фон мира (не модифицируем)
        } catch (Exception e) {
            // Если файл не найден — рисуем простую трассу программно
            trackImage = createFallbackTrack();
            setBackground(new GreenfootImage(trackImage)); // копия для фона
        }
    }

    /**
     * Создаёт простейший фоллбэк-трек программно, если track_1.png отсутствует.
     * Серый прямоугольник-кольцо на зелёном фоне.
     */
    private GreenfootImage createFallbackTrack() {
        GreenfootImage img = new GreenfootImage(800, 600);

        // Фон — зелёный (трава / off-road)
        img.setColor(new Color(34, 139, 34));
        img.fill();

        // Внешний контур трека — тёмно-серый
        img.setColor(new Color(100, 100, 100));
        img.fillOval(50, 50, 700, 500);

        // Внутренний вырез — зелёный
        img.setColor(new Color(34, 139, 34));
        img.fillOval(150, 130, 500, 340);

        // Белая финишная черта
        img.setColor(Color.WHITE);
        img.fillRect(330, 460, 60, 8);

        return img;
    }

    // ---------------------------------------------------------------
    //  Создание чекпоинтов — возвращает список для LapTimer
    // ---------------------------------------------------------------

    /**
     * Создаёт чекпоинты и добавляет их в мир.
     * Координаты соответствуют трассе (кольцу).
     *
     * @return список чекпоинтов в порядке прохождения (order: 0, 1, 2, ...)
     */
    public List<Checkpoint> setupCheckpoints() {
        // Координаты чекпоинтов по кольцу трека (по часовой стрелке)
        int[][] positions = {
            {400, 470},  // 0 — стартовый (чуть выше финишной черты)
            {680, 300},  // 1 — правая часть
            {400, 100},  // 2 — верхняя часть
            {120, 300},  // 3 — левая часть
        };

        List<Checkpoint> list = new ArrayList<>();
        for (int i = 0; i < positions.length; i++) {
            Checkpoint cp = new Checkpoint(i);
            addObject(cp, positions[i][0], positions[i][1]);
            list.add(cp);
        }
        return list;
    }

    // ---------------------------------------------------------------
    //  Главный цикл мира
    // ---------------------------------------------------------------

    @Override
    public void act() {
        if (raceFinished) return;

        // Обновляем менеджер (проверяет финиши)
        raceManager.update();

        // Проверяем offRoad для каждой машины
        checkOffRoad();

        // Обновляем HUD
        drawHUD();

        // Если гонка завершилась — фиксируем
        if (raceManager.isRaceFinished()) {
            raceFinished = true;
        }
    }

    // ---------------------------------------------------------------
    //  Проверка offRoad по цвету пикселя
    // ---------------------------------------------------------------

    /**
     * Для каждой машины проверяет цвет пикселя трека под её позицией.
     * Если цвет не соответствует дороге — вызывает car.setOffRoad(true).
     */
    private void checkOffRoad() {
        @SuppressWarnings("unchecked")
        List<Car> cars = (List<Car>) getObjects(Car.class);
        if (cars == null) return;

        for (Car car : cars) {
            int x = car.getX();
            int y = car.getY();

            // Проверяем границы
            if (x < 0 || y < 0 || x >= trackImage.getWidth() || y >= trackImage.getHeight()) {
                car.setOffRoad(true);
                continue;
            }

            Color pixel = trackImage.getColorAt(x, y);
            car.setOffRoad(!isRoadPixel(pixel));
        }
    }

    /**
     * Определяет, является ли пиксель дорогой.
     * Дорога — оттенок серого в определённом диапазоне.
     */
    private boolean isRoadPixel(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        // Серый = R ≈ G ≈ B
        int maxDiff = Math.max(Math.abs(r - g), Math.max(Math.abs(r - b), Math.abs(g - b)));
        if (maxDiff > ROAD_DIFF) return false;

        // В нужном диапазоне яркости
        int avg = (r + g + b) / 3;
        return avg >= ROAD_MIN && avg <= ROAD_MAX;
    }

    // ---------------------------------------------------------------
    //  HUD (отображение информации)
    // ---------------------------------------------------------------

    /**
     * Рисует простой HUD поверх фона.
     * Отображает: текущий круг и лучшее время круга для первого игрока.
     */
    private void drawHUD() {
        LapTimer timer = raceManager.getLapTimer();
        if (timer == null) return;

        List<PlayerCar> players = raceManager.getPlayers();
        if (players == null || players.isEmpty()) return;

        GreenfootImage bg = getBackground();

        // Сбрасываем HUD-область (перерисовываем фон в прямоугольнике HUD)
        // Для простоты рисуем полупрозрачный прямоугольник
        GreenfootImage hudPanel = new GreenfootImage(HUD_WIDTH, HUD_LINE_H * 4 + 10);
        hudPanel.setColor(new Color(0, 0, 0, 160));
        hudPanel.fill();
        bg.drawImage(hudPanel, HUD_X, HUD_Y);

        bg.setColor(Color.WHITE);
        bg.setFont(new Font("Arial", false, false, 14));

        for (int i = 0; i < players.size(); i++) {
            PlayerCar p = players.get(i);
            int lap     = timer.getCurrentLap(p);
            long best   = timer.getBestLapTime(p);
            long total  = timer.getTotalRaceTime(p);

            int yOff = HUD_Y + 12 + i * (HUD_LINE_H * 3);

            String header   = "Игрок " + p.getPlayerNumber();
            String lapStr   = "Круг: " + Math.min(lap, timer.getTotalLaps()) + "/" + timer.getTotalLaps();
            String bestStr  = "Лучший: " + RaceResult.formatTime(best);
            String totalStr = "Время: " + RaceResult.formatTime(total);

            bg.drawString(header,   HUD_X + 5, yOff);
            bg.drawString(lapStr,   HUD_X + 5, yOff + HUD_LINE_H);
            bg.drawString(bestStr,  HUD_X + 5, yOff + HUD_LINE_H * 2);
            bg.drawString(totalStr, HUD_X + 5, yOff + HUD_LINE_H * 3);
        }

        setBackground(bg);
    }

    // ---------------------------------------------------------------
    //  Геттеры и сеттеры
    // ---------------------------------------------------------------

    public RaceManager getRaceManager()  { return raceManager; }
    public int         getGameMode()     { return gameMode; }
    public boolean     isRaceFinished()  { return raceFinished; }
    public void        setRaceFinished(boolean v) { this.raceFinished = v; }
}
