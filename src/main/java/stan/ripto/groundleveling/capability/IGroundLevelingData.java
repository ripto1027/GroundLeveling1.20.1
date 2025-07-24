package stan.ripto.groundleveling.capability;

public interface IGroundLevelingData {
    int getMode();
    void changeMode();
    void setMode(int value);

    boolean isSynced();
    void setSynced(boolean value);

    boolean isInProcessing();
    void setInProcessing(boolean value);
}
