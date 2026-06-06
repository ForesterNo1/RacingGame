import greenfoot.*;

/**
 * Главное меню игры.
 *
 * Управление:
 *   Клавиши 1/2/3 — выбор режима игры.
 *   +/-            — изменить количество кругов (1–10).
 *   Enter          — начать гонку.
 *
 * Режимы:
 *   1 → Против ботов (1 игрок vs 5 ботов)
 *   2 → На время     (1 игрок)
 *   3 → 2 Игрока     (один компьютер)
 */
public class MenuWorld extends World {

    // ---------------------------------------------------------------
    //  Константы
    // ---------------------------------------------------------------

    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    private static final int MIN_LAPS = 1;
    private static final int MAX_LAPS = 10;

    private static final String[] MODE_NAMES = {
        "1. Против ботов",
        "2. На время",
        "3. Два игрока"
    };

    // Цвета
    private static final Color COLOR_BG       = new Color(15, 15, 30);
    private static final Color COLOR_TITLE     = new Color(255, 220, 50);
    private static final Color COLOR_SELECTED  = new Color(80, 220, 120);
    private static final Color COLOR_NORMAL    = new Color(200, 200, 200);
    private static final Color COLOR_DIM       = new Color(120, 120, 140);
    private static final Color COLOR_HINT      = new Color(160, 160, 180);
    private static final Color COLOR_OVERLAY   = new Color(0, 0, 0, 120);

    // ---------------------------------------------------------------
    //  Поля состояния
    // ---------------------------------------------------------------

    private int selectedMode = RaceWorld.MODE_VS_BOTS; // текущий выбранный режим
    private int totalLaps    = 3;                      // количество кругов

    /** Флаг для предотвращения повторных нажатий (ждём отпускания клавиши). */
    private String lastKey = "";

    // ---------------------------------------------------------------
    //  Конструктор
    // ---------------------------------------------------------------

    public MenuWorld() {
        super(WIDTH, HEIGHT, 1);
        Greenfoot.setSpeed(50);
        drawMenu();
    }

    // ---------------------------------------------------------------
    //  Главный цикл
    // ---------------------------------------------------------------

    @Override
    public void act() {
        String key = Greenfoot.getKey(); // getKey() возвращает клавишу один раз
        if (key == null) return;

        boolean changed = false;

        switch (key) {
            case "1":
                selectedMode = RaceWorld.MODE_VS_BOTS;
                changed = true;
                break;
            case "2":
                selectedMode = RaceWorld.MODE_TIME_TRIAL;
                changed = true;
                break;
            case "3":
                selectedMode = RaceWorld.MODE_TWO_PLAYER;
                changed = true;
                break;
            case "equals": // клавиша "=" / "+"
            case "plus":
                if (totalLaps < MAX_LAPS) { totalLaps++; changed = true; }
                break;
            case "minus":
                if (totalLaps > MIN_LAPS) { totalLaps--; changed = true; }
                break;
            case "enter":
                startRace();
                return;
        }

        if (changed) {
            drawMenu();
        }
    }

    // ---------------------------------------------------------------
    //  Запуск гонки
    // ---------------------------------------------------------------

    private void startRace() {
        Greenfoot.setWorld(new RaceWorld(selectedMode, totalLaps));
    }

    // ---------------------------------------------------------------
    //  Отрисовка меню
    // ---------------------------------------------------------------

    private void drawMenu() {
        GreenfootImage img = new GreenfootImage(WIDTH, HEIGHT);

        // Фон
        img.setColor(COLOR_BG);
        img.fill();

        // Декоративные полосы
        img.setColor(new Color(30, 30, 60));
        for (int y = 0; y < HEIGHT; y += 40) {
            img.fillRect(0, y, WIDTH, 2);
        }

        // Заголовок
        drawCenteredText(img, "ГОНКИ", 130, new Font("Arial", true, false, 72), COLOR_TITLE);
        drawCenteredText(img, "Top-Down Racing", 175, new Font("Arial", false, true, 22), COLOR_DIM);

        // Разделитель
        img.setColor(COLOR_TITLE);
        img.fillRect(200, 195, 400, 2);

        // Выбор режима
        drawCenteredText(img, "Выберите режим:", 240, new Font("Arial", false, false, 18), COLOR_HINT);

        int yMode = 275;
        for (int i = 0; i < MODE_NAMES.length; i++) {
            boolean isSelected = (i == selectedMode);
            Color c = isSelected ? COLOR_SELECTED : COLOR_NORMAL;
            Font  f = new Font("Arial", isSelected, false, isSelected ? 22 : 20);

            String prefix = isSelected ? "▶ " : "  ";
            drawCenteredText(img, prefix + MODE_NAMES[i], yMode + i * 42, f, c);
        }

        // Разделитель
        img.setColor(new Color(50, 50, 80));
        img.fillRect(200, 405, 400, 1);

        // Количество кругов
        drawCenteredText(img, "Кругов: " + totalLaps, 440,
            new Font("Arial", true, false, 24), COLOR_SELECTED);
        drawCenteredText(img, "Изменить: клавиши  +  /  -", 468,
            new Font("Arial", false, false, 14), COLOR_DIM);

        // Подсказки
        img.setColor(new Color(40, 40, 70));
        img.fillRect(0, HEIGHT - 80, WIDTH, 80);

        drawCenteredText(img, "Клавиши 1 / 2 / 3 — режим     Enter — старт", HEIGHT - 50,
            new Font("Arial", false, false, 15), COLOR_HINT);

        // Описание текущего режима
        String desc = getModeDescription(selectedMode);
        drawCenteredText(img, desc, HEIGHT - 25,
            new Font("Arial", false, true, 13), COLOR_DIM);

        setBackground(img);
    }

    // ---------------------------------------------------------------
    //  Вспомогательные методы отрисовки
    // ---------------------------------------------------------------

    private void drawCenteredText(GreenfootImage img, String text, int y, Font font, Color color) {
        GreenfootImage tmp = new GreenfootImage(text, font.getSize(), color, new Color(0,0,0,0));
        int x = (WIDTH - tmp.getWidth()) / 2;
        img.drawImage(tmp, x, y - tmp.getHeight() / 2);
    }

    private String getModeDescription(int mode) {
        switch (mode) {
            case RaceWorld.MODE_VS_BOTS:
                return "Стрелки — управление. Обгони 5 ботов!";
            case RaceWorld.MODE_TIME_TRIAL:
                return "Стрелки — управление. Установи лучшее время!";
            case RaceWorld.MODE_TWO_PLAYER:
                return "Игрок 1: Стрелки  |  Игрок 2: W/A/S/D";
            default:
                return "";
        }
    }
}
