package stan.ripto.groundleveling.capability;

public class GroundLevelingData implements IGroundLevelingData{
    private int mode = 0;

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void changeMode() {
        if (mode == 2) {
            mode = 0;
        } else {
            mode++;
        }
    }

    @Override
    public void setMode(int value) {
        if (0 <= value && value <= 2) {
            mode = value;
        } else {
            mode = 0;
        }
    }
}
