/**
 * Неизменяемый объект данных (DTO) с результатом одного участника.
 * Передаётся из RaceManager в ResultWorld.
 */
public class RaceResult {

    private final int    place;       // 1-е, 2-е, ... место
    private final String name;        // "Игрок 1", "Бот (Сложный)" и т.д.
    private final long   totalTimeMs; // суммарное время гонки (мс)
    private final long   bestLapMs;   // лучшее время круга (мс), -1 если нет данных

    public RaceResult(int place, String name, long totalTimeMs, long bestLapMs) {
        this.place       = place;
        this.name        = name;
        this.totalTimeMs = totalTimeMs;
        this.bestLapMs   = bestLapMs;
    }

    // ---------------------------------------------------------------
    //  Форматирование времени
    // ---------------------------------------------------------------

    /**
     * Форматирует миллисекунды в строку "М:СС.мм".
     */
    public static String formatTime(long ms) {
        if (ms < 0) return "--:--.--";
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long hundredths = (ms % 1000) / 10;
        return String.format("%d:%02d.%02d", minutes, seconds, hundredths);
    }

    // ---------------------------------------------------------------
    //  Геттеры
    // ---------------------------------------------------------------

    public int    getPlace()       { return place; }
    public String getName()        { return name; }
    public long   getTotalTimeMs() { return totalTimeMs; }
    public long   getBestLapMs()   { return bestLapMs; }

    public String getTotalTimeStr() { return formatTime(totalTimeMs); }
    public String getBestLapStr()   { return formatTime(bestLapMs); }
}
