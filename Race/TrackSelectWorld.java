import greenfoot.*;

/**
 * Экран выбора трассы.
 * Фон: Choosetrack.png (1920x1080).
 *
 * Три трассы: Игора Драйв, Silverstone, Сочи.
 * Файлы фонов трасс: Igora.png, Silverstone.png, Sochi.png
 *
 * После выбора → RaceWorld
 */
public class TrackSelectWorld extends World {

    private static final int W = 1280;
    private static final int H = 720;

    // -------------------------------------------------------------------
    //  Константы трасс
    // -------------------------------------------------------------------

    public static final int TRACK_IGORA       = 0;
    public static final int TRACK_SILVERSTONE = 1;
    public static final int TRACK_SOCHI       = 2;

    public static final String[] TRACK_FILES = {
        "images/Assets/Tracks/Igora.png",
        "images/Assets/Tracks/Silverstone.png",
        "images/Assets/Tracks/Sochi.png"
    };

    // -------------------------------------------------------------------
    //  Зоны кнопок трасс
    //  Три карточки горизонтально, подобраны под Choosetrack.png (1920x1080)
    //  Аналогичная раскладка как в TeamSelect
    // -------------------------------------------------------------------

    private static final int CARD_Y = 185;
    private static final int CARD_H = 307;
    private static final int CARD_W = 293;

    private static final int CARD_IGORA_X       = 50;
    private static final int CARD_SILVERSTONE_X = 493;
    private static final int CARD_SOCHI_X       = 937;

    // -------------------------------------------------------------------
    //  Поля — пробрасываем выбор команд из предыдущих экранов
    // -------------------------------------------------------------------

    private final int gameMode;
    private final int player1Team;  // команда игрока 1
    private final int player2Team;  // команда игрока 2 (-1 если не PvP)

    public TrackSelectWorld(int gameMode, int player1Team, int player2Team) {
        super(W, H, 1);
        this.gameMode    = gameMode;
        this.player1Team = player1Team;
        this.player2Team = player2Team;
        setBackground("images/Assets/Menu/Choosetrack.png");
    }

    @Override
    public void act() {
        if (Greenfoot.mouseClicked(null)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse == null) return;

            int mx = mouse.getX();
            int my = mouse.getY();

            int track = -1;
            if (inCard(mx, my, CARD_IGORA_X))       track = TRACK_IGORA;
            else if (inCard(mx, my, CARD_SILVERSTONE_X)) track = TRACK_SILVERSTONE;
            else if (inCard(mx, my, CARD_SOCHI_X))   track = TRACK_SOCHI;

            if (track != -1) {
                startRace(track);
            }
        }
    }

    private void startRace(int track) {
        int laps;
        if (gameMode == RaceWorld.MODE_TIME_TRIAL) {
            laps = 2; // 1 прогревочный + 1 зачётный (логика в RaceManager)
        } else {
            laps = 3;
        }
        Greenfoot.setWorld(new RaceWorld(gameMode, laps, track, player1Team, player2Team));
    }

    private boolean inCard(int mx, int my, int cardX) {
        return mx >= cardX && mx <= cardX + CARD_W
            && my >= CARD_Y && my <= CARD_Y + CARD_H;
    }
}
