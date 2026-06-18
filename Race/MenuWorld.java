import greenfoot.*;

/**
 * Главное меню игры.
 * Фон: Mainmenu.png (1920x1080).
 * Навигация: мышь.
 *
 * Кнопки (координаты кликабельных зон подобраны под спрайт Mainmenu.png):
 *   Race  → TeamSelectWorld(MODE_VS_BOTS)
 *   Time  → TeamSelectWorld(MODE_TIME_TRIAL)
 *   PvP   → TeamSelectWorld(MODE_TWO_PLAYER)
 *   Exit  → Greenfoot.stop()
 */
public class MenuWorld extends World {

    private static final int W = 1280;
    private static final int H = 720;

    // Зоны кнопок [x, y, ширина, высота] — центр кнопки по спрайту Mainmenu.png
    // Координаты масштабированы с оригинала 1920x1080 (коэффициент 0.6667)
    private static final int BTN_X  = 527;   // левый край кнопок
    private static final int BTN_W  = 225;   // ширина кнопки
    private static final int BTN_H  = 55;    // высота кнопки

    private static final int BTN_RACE_Y  = 400;
    private static final int BTN_TIME_Y  = 470;
    private static final int BTN_PVP_Y   = 540;
    private static final int BTN_EXIT_Y  = 605;

    private boolean mouseWasDown = false;

    public MenuWorld() {
        super(W, H, 1);
        Greenfoot.setSpeed(50);
        setBackground("images/Assets/Menu/Mainmenu.png");
        prepare();
    }

    @Override
    public void act() {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        boolean mouseDown = Greenfoot.mousePressed(null);

        // Срабатывание по отпусканию кнопки мыши (клик)
        if (Greenfoot.mouseClicked(null) && mouse != null) {
            int mx = mouse.getX();
            int my = mouse.getY();

            if (inButton(mx, my, BTN_RACE_Y)) {
                Greenfoot.setWorld(new TeamSelectWorld(RaceWorld.MODE_VS_BOTS));
            } else if (inButton(mx, my, BTN_TIME_Y)) {
                Greenfoot.setWorld(new TeamSelectWorld(RaceWorld.MODE_TIME_TRIAL));
            } else if (inButton(mx, my, BTN_PVP_Y)) {
                Greenfoot.setWorld(new TeamSelectWorld(RaceWorld.MODE_TWO_PLAYER));
            } else if (inButton(mx, my, BTN_EXIT_Y)) {
                Greenfoot.stop();
            }
        }
    }

    /** Проверяет, попадает ли точка (mx, my) в кнопку с заданным Y. */
    private boolean inButton(int mx, int my, int btnY) {
        return mx >= BTN_X && mx <= BTN_X + BTN_W
        && my >= btnY  && my <= btnY + BTN_H;
    }
    /**
     * Prepare the world for the start of the program.
     * That is: create the initial objects and add them to the world.
     */
    private void prepare()
    {
    }
}
