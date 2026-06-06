import greenfoot.*;
import java.util.List;

/**
 * Экран результатов гонки.
 *
 * Отображает:
 *   - Место, имя участника, суммарное время, лучший круг.
 *
 * Управление:
 *   Любая клавиша / пробел / Enter → возврат в MenuWorld.
 */
public class ResultWorld extends World {

    // ---------------------------------------------------------------
    //  Константы
    // ---------------------------------------------------------------

    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    // Цвета (совпадают с MenuWorld для единого стиля)
    private static final Color COLOR_BG      = new Color(15, 15, 30);
    private static final Color COLOR_TITLE   = new Color(255, 220, 50);
    private static final Color COLOR_GOLD    = new Color(255, 215, 0);
    private static final Color COLOR_SILVER  = new Color(192, 192, 192);
    private static final Color COLOR_BRONZE  = new Color(205, 127, 50);
    private static final Color COLOR_NORMAL  = new Color(200, 200, 220);
    private static final Color COLOR_HEADER  = new Color(160, 160, 190);
    private static final Color COLOR_HINT    = new Color(130, 130, 155);
    private static final Color COLOR_DIM     = new Color(80, 80, 100);
    private static final Color COLOR_PLAYER  = new Color(80, 200, 255);  // синий — игрок
    private static final Color COLOR_BOT     = new Color(200, 200, 200); // белый — бот

    // Координаты таблицы
    private static final int TABLE_START_X = 60;
    private static final int TABLE_START_Y = 200;
    private static final int ROW_HEIGHT    = 46;

    // Ширина колонок (сдвиги от TABLE_START_X)
    private static final int COL_PLACE = 0;
    private static final int COL_NAME  = 70;
    private static final int COL_TIME  = 430;
    private static final int COL_BEST  = 620;

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final List<RaceResult> results;
    private final int              gameMode;

    // ---------------------------------------------------------------
    //  Конструктор
    // ---------------------------------------------------------------

    /**
     * @param results  список результатов из RaceManager (отсортированный по месту)
     * @param gameMode режим гонки (для отображения заголовка)
     */
    public ResultWorld(List<RaceResult> results, int gameMode) {
        super(WIDTH, HEIGHT, 1);
        this.results  = results;
        this.gameMode = gameMode;
        drawResults();
    }

    // ---------------------------------------------------------------
    //  Главный цикл
    // ---------------------------------------------------------------

    @Override
    public void act() {
        // Любое нажатие клавиши возвращает в меню
        if (Greenfoot.getKey() != null) {
            Greenfoot.setWorld(new MenuWorld());
        }
    }

    // ---------------------------------------------------------------
    //  Отрисовка экрана результатов
    // ---------------------------------------------------------------

    private void drawResults() {
        GreenfootImage img = new GreenfootImage(WIDTH, HEIGHT);

        // Фон
        img.setColor(COLOR_BG);
        img.fill();

        // Декоративные полосы (как в меню)
        img.setColor(new Color(25, 25, 50));
        for (int y = 0; y < HEIGHT; y += 40) {
            img.fillRect(0, y, WIDTH, 1);
        }

        // Заголовок
        drawText(img, "РЕЗУЛЬТАТЫ", WIDTH / 2, 60,
            new Font("Arial", true, false, 52), COLOR_TITLE, true);

        String modeStr = getModeString(gameMode);
        drawText(img, modeStr, WIDTH / 2, 105,
            new Font("Arial", false, true, 18), COLOR_HEADER, true);

        // Разделитель
        img.setColor(COLOR_TITLE);
        img.fillRect(80, 125, WIDTH - 160, 2);

        // Заголовок таблицы
        img.setColor(new Color(30, 30, 60));
        img.fillRect(TABLE_START_X - 10, TABLE_START_Y - 34, WIDTH - 2 * TABLE_START_X + 20, 28);

        Font headerFont = new Font("Arial", true, false, 14);
        drawText(img, "Место", TABLE_START_X + COL_PLACE + 20, TABLE_START_Y - 22, headerFont, COLOR_HEADER, false);
        drawText(img, "Участник",     TABLE_START_X + COL_NAME,  TABLE_START_Y - 22, headerFont, COLOR_HEADER, false);
        drawText(img, "Общее время",  TABLE_START_X + COL_TIME,  TABLE_START_Y - 22, headerFont, COLOR_HEADER, false);
        drawText(img, "Лучший круг",  TABLE_START_X + COL_BEST,  TABLE_START_Y - 22, headerFont, COLOR_HEADER, false);

        // Строки результатов
        for (int i = 0; i < results.size(); i++) {
            drawResultRow(img, results.get(i), i);
        }

        // Нижняя подсказка
        img.setColor(new Color(30, 30, 55));
        img.fillRect(0, HEIGHT - 65, WIDTH, 65);

        drawText(img, "Нажмите любую клавишу для возврата в меню", WIDTH / 2, HEIGHT - 35,
            new Font("Arial", false, false, 15), COLOR_HINT, true);

        setBackground(img);
    }

    // ---------------------------------------------------------------
    //  Отрисовка строки результата
    // ---------------------------------------------------------------

    private void drawResultRow(GreenfootImage img, RaceResult result, int rowIndex) {
        int y = TABLE_START_Y + rowIndex * ROW_HEIGHT;

        // Чередование фона строк
        Color rowBg = (rowIndex % 2 == 0)
            ? new Color(20, 20, 45)
            : new Color(25, 25, 55);
        img.setColor(rowBg);
        img.fillRect(TABLE_START_X - 10, y - 18, WIDTH - 2 * TABLE_START_X + 20, ROW_HEIGHT - 4);

        // Цвет строки по месту
        Color placeColor;
        switch (result.getPlace()) {
            case 1:  placeColor = COLOR_GOLD;   break;
            case 2:  placeColor = COLOR_SILVER; break;
            case 3:  placeColor = COLOR_BRONZE; break;
            default: placeColor = COLOR_NORMAL; break;
        }

        // Цвет имени: синий для игроков, белый для ботов
        boolean isPlayer = result.getName().startsWith("Игрок");
        Color nameColor  = isPlayer ? COLOR_PLAYER : COLOR_BOT;

        Font dataFont   = new Font("Arial", false, false, 16);
        Font boldFont   = new Font("Arial", true,  false, 18);

        // Место
        String placeStr = result.getPlace() + ".";
        if (result.getPlace() <= 3) placeStr = getMedalEmoji(result.getPlace()) + " " + placeStr;
        drawText(img, placeStr, TABLE_START_X + COL_PLACE, y, boldFont, placeColor, false);

        // Имя
        drawText(img, result.getName(), TABLE_START_X + COL_NAME, y, dataFont, nameColor, false);

        // Общее время
        drawText(img, result.getTotalTimeStr(), TABLE_START_X + COL_TIME, y, dataFont, COLOR_NORMAL, false);

        // Лучший круг
        drawText(img, result.getBestLapStr(), TABLE_START_X + COL_BEST, y, dataFont, COLOR_DIM, false);
    }

    // ---------------------------------------------------------------
    //  Вспомогательные методы
    // ---------------------------------------------------------------

    private String getMedalEmoji(int place) {
        switch (place) {
            case 1: return "1";
            case 2: return "2";
            case 3: return "3";
            default: return "";
        }
    }

    private String getModeString(int mode) {
        switch (mode) {
            case RaceWorld.MODE_VS_BOTS:    return "Режим: Против ботов";
            case RaceWorld.MODE_TIME_TRIAL: return "Режим: Гонка на время";
            case RaceWorld.MODE_TWO_PLAYER: return "Режим: Два игрока";
            default:                        return "";
        }
    }

    /**
     * Рисует текст с опциональным выравниванием по центру.
     *
     * @param img        изображение для рисования
     * @param text       текст
     * @param x          X-позиция (левый край или центр, если centered=true)
     * @param y          Y-позиция (вертикальный центр текста)
     * @param font       шрифт
     * @param color      цвет
     * @param centered   true — выравнивать по центру относительно x
     */
    private void drawText(GreenfootImage img, String text, int x, int y,
                          Font font, Color color, boolean centered) {
        if (text == null || text.isEmpty()) return;
        GreenfootImage tmp = new GreenfootImage(text, font.getSize(), color, new Color(0,0,0,0));
        int drawX = centered ? (x - tmp.getWidth() / 2) : x;
        int drawY = y - tmp.getHeight() / 2;
        img.drawImage(tmp, drawX, drawY);
    }
}
