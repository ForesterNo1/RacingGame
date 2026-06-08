import greenfoot.*;
import java.util.List;

/**
 * Экран результатов гонки.
 *
 * Выбор фона:
 *   MODE_VS_BOTS    → Win.png     (текст "Ваше место:")
 *   MODE_TIME_TRIAL → Timewin.png (текст "Ваше время:")
 *   MODE_TWO_PLAYER → P1win.png или P2win.png (в зависимости от победителя)
 *
 * Кнопка "В меню" — навигация мышью.
 * Размер: 1920x1080.
 */
public class ResultWorld extends World {

    private static final int W = 1920;
    private static final int H = 1080;

    // Зона кнопки "В меню" — подобрана под спрайты Win/Timewin/P1win/P2win
    // Кнопка по центру экрана, примерно y=600..680
    private static final int BTN_W = 340;
    private static final int BTN_H = 80;
    private static final int BTN_X = (W - BTN_W) / 2;   // центр по X
    private static final int BTN_Y = 600;

    // ---------------------------------------------------------------
    //  Поля
    // ---------------------------------------------------------------

    private final List<RaceResult> results;
    private final int              gameMode;

    // ---------------------------------------------------------------
    //  Конструктор
    // ---------------------------------------------------------------

    public ResultWorld(List<RaceResult> results, int gameMode) {
        super(W, H, 1);
        this.results  = results;
        this.gameMode = gameMode;
        setupBackground();
    }

    // ---------------------------------------------------------------
    //  Выбор и настройка фона
    // ---------------------------------------------------------------

    private void setupBackground() {
        String bgFile = chooseBgFile();
        GreenfootImage bg;
        try {
            bg = new GreenfootImage(bgFile);
            bg.scale(W, H);
        } catch (Exception e) {
            bg = new GreenfootImage(W, H);
            bg.setColor(new Color(15, 15, 30));
            bg.fill();
        }

        // Дорисовываем данные поверх фона
        drawResultData(bg);
        setBackground(bg);
    }

    private String chooseBgFile() {
        switch (gameMode) {
            case RaceWorld.MODE_TIME_TRIAL:
                return "Timewin.png";
            case RaceWorld.MODE_TWO_PLAYER:
                // Победитель — игрок с местом 1
                if (!results.isEmpty()) {
                    String winner = results.get(0).getName();
                    return winner.contains("2") ? "P2win.png" : "P1win.png";
                }
                return "P1win.png";
            default: // MODE_VS_BOTS
                return "Win.png";
        }
    }

    // ---------------------------------------------------------------
    //  Отрисовка данных поверх спрайта
    // ---------------------------------------------------------------

    private void drawResultData(GreenfootImage bg) {
        if (results.isEmpty()) return;

        RaceResult top = results.get(0); // победитель / единственный игрок

        // Позиция текста с результатом — по центру экрана, ниже надписи на спрайте
        // Спрайт Win.png: "Ваше место:" ~ y=350, пусто ниже ~ y=450
        // Спрайт Timewin.png: "Ваше время:" ~ y=350
        // Спрайты P1win/P2win: имя ~ y=300

        int textY = 460;  // Y для результата (подогнать если нужно)
        Font font = new Font("Arial", true, false, 52);

        String value;
        switch (gameMode) {
            case RaceWorld.MODE_TIME_TRIAL:
                // Показываем лучшее время зачётного круга
                value = RaceResult.formatTime(top.getBestLapMs());
                break;
            case RaceWorld.MODE_TWO_PLAYER:
                // Победитель уже показан фоном; дополнительный текст не нужен
                value = null;
                break;
            default: // VS_BOTS
                value = top.getPlace() + " место";
                break;
        }

        if (value != null) {
            drawCentered(bg, value, textY, font, new Color(60, 30, 10));
        }
    }

    // ---------------------------------------------------------------
    //  Главный цикл — ожидаем клик по кнопке "В меню"
    // ---------------------------------------------------------------

    @Override
    public void act() {
        if (Greenfoot.mouseClicked(null)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse == null) return;
            int mx = mouse.getX();
            int my = mouse.getY();

            if (mx >= BTN_X && mx <= BTN_X + BTN_W
             && my >= BTN_Y && my <= BTN_Y + BTN_H) {
                Greenfoot.setWorld(new MenuWorld());
            }
        }
    }

    // ---------------------------------------------------------------
    //  Вспомогательные методы
    // ---------------------------------------------------------------

    private void drawCentered(GreenfootImage img, String text, int y,
                               Font font, Color color) {
        GreenfootImage tmp = new GreenfootImage(text, font.getSize(),
                                                color, new Color(0, 0, 0, 0));
        int x = (W - tmp.getWidth()) / 2;
        img.drawImage(tmp, x, y - tmp.getHeight() / 2);
    }
}
