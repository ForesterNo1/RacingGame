import greenfoot.*;

/**
 * Экран выбора команды.
 *
 * Режимы:
 *   - MODE_VS_BOTS / MODE_TIME_TRIAL : один игрок выбирает команду → TrackSelectWorld
 *   - MODE_TWO_PLAYER, шаг 1         : Игрок 1 выбирает → тот же экран, шаг 2
 *   - MODE_TWO_PLAYER, шаг 2         : Игрок 2 выбирает (команда игрока 1 заблокирована) → TrackSelectWorld
 *
 * Фоны:
 *   - Шаг 1 одиночный / P1 в PvP : Chooseteam.png  (или ChooseTaemP1.png для PvP)
 *   - Шаг 2 в PvP                 : ChooseTaemP2.png
 *
 * Команды (кнопки):
 *   Aston Martin, Ferrari, Red Bull — горизонтально по центру экрана.
 *   Файлы спрайтов: Astonmartin.png, Ferrari.png, Redbull.png
 */
public class TeamSelectWorld extends World {

    private static final int W = 1280;
    private static final int H = 720;

    // -------------------------------------------------------------------
    //  Константы команд
    // -------------------------------------------------------------------

    public static final int TEAM_ASTON  = 0;
    public static final int TEAM_FERRARI = 1;
    public static final int TEAM_REDBULL = 2;

    public static final String[] TEAM_SPRITES = {
        "images/Assets/Cars/Astonmartin.png",
        "images/Assets/Cars/Ferrari.png",
        "images/Assets/Cars/Redbull.png"
    };

    // Названия команд (для передачи в RaceManager)
    public static final String[] TEAM_NAMES = {
        "Aston Martin", "Ferrari", "Red Bull"
    };

    // -------------------------------------------------------------------
    //  Зоны кнопок команд
    //  Три карточки горизонтально, подобраны под Chooseteam.png (1920x1080)
    //  Карточки примерно: x=75, x=660, x=1245 (по левому краю), y=280..750
    // -------------------------------------------------------------------

    private static final int CARD_Y = 185;
    private static final int CARD_H = 307;
    private static final int CARD_W = 293;

    private static final int CARD_ASTON_X  = 50;
    private static final int CARD_FERRARI_X = 493;
    private static final int CARD_REDBULL_X = 937;

    // -------------------------------------------------------------------
    //  Поля
    // -------------------------------------------------------------------

    private final int gameMode;
    private final int pvpStep;          // 1 = выбирает игрок 1, 2 = выбирает игрок 2
    private final int player1Team;      // -1 если ещё не выбран

    // Команда заблокирована (выбрана другим игроком в PvP)
    private final int blockedTeam;

    public TeamSelectWorld(int gameMode) {
        this(gameMode, 1, -1);
    }

    /**
     * @param gameMode    режим игры
     * @param pvpStep     1 или 2 (только для PvP)
     * @param player1Team команда игрока 1 (-1 если не выбрана)
     */
    public TeamSelectWorld(int gameMode, int pvpStep, int player1Team) {
        super(W, H, 1);
        this.gameMode    = gameMode;
        this.pvpStep     = pvpStep;
        this.player1Team = player1Team;
        this.blockedTeam = (gameMode == RaceWorld.MODE_TWO_PLAYER && pvpStep == 2)
                           ? player1Team : -1;

        // Выбираем фон
        String bg;
        if (gameMode == RaceWorld.MODE_TWO_PLAYER) {
            bg = (pvpStep == 1) ? "images/Assets/Menu/ChooseTaemP1.png" : "images/Assets/Menu/ChooseTaemP2.png";
        } else {
            bg = "images/Assets/Menu/Chooseteam.png";
        }
        setBackground(bg);
    }

    @Override
    public void act() {
        if (Greenfoot.mouseClicked(null)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse == null) return;

            int mx = mouse.getX();
            int my = mouse.getY();

            int chosen = -1;
            if (inCard(mx, my, CARD_ASTON_X))   chosen = TEAM_ASTON;
            else if (inCard(mx, my, CARD_FERRARI_X)) chosen = TEAM_FERRARI;
            else if (inCard(mx, my, CARD_REDBULL_X)) chosen = TEAM_REDBULL;

            if (chosen == -1) return;           // клик мимо
            if (chosen == blockedTeam) return;  // заблокированная команда

            onTeamChosen(chosen);
        }
    }

    private void onTeamChosen(int team) {
        if (gameMode == RaceWorld.MODE_TWO_PLAYER) {
            if (pvpStep == 1) {
                // Игрок 1 выбрал — переходим к шагу 2
                Greenfoot.setWorld(new TeamSelectWorld(gameMode, 2, team));
            } else {
                // Игрок 2 выбрал — оба выбрали, идём к выбору трассы
                Greenfoot.setWorld(new TrackSelectWorld(gameMode, player1Team, team));
            }
        } else {
            // Одиночный режим — один игрок, сразу к трассе
            Greenfoot.setWorld(new TrackSelectWorld(gameMode, team, -1));
        }
    }

    private boolean inCard(int mx, int my, int cardX) {
        return mx >= cardX && mx <= cardX + CARD_W
            && my >= CARD_Y && my <= CARD_Y + CARD_H;
    }
}
