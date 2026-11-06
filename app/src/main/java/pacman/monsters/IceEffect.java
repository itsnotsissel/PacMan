package pacman.monsters;

public abstract class IceEffect implements MonsterEffect {
    private int tickRemaining;

    protected IceEffect(int durationTicks) {
        this.tickRemaining = durationTicks;
    }

    @Override
    public void tick() {
        tickRemaining--;
    }

    @Override
    public boolean isExpired() {
        return tickRemaining <= 0;
    }
}
