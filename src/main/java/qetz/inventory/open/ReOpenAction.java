package qetz.inventory.open;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import qetz.inventory.InventoryAction;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @Inject)
public final class ReOpenAction implements InventoryAction {
  private static final ReOpenAction lazy = new ReOpenAction();

  public static ReOpenAction lazy() {
    return lazy;
  }

  @Override
  public ExecutableAction asExecutable() {
    return new ReOpenExecutable();
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class ReOpenExecutable implements ExecutableAction {
    private OpenInventory inventory;

    @Override
    public ExecutableAction withTarget(OpenInventory inventory) {
      this.inventory = inventory;
      return this;
    }

    @Override
    public void perform() {
      Preconditions.checkNotNull(inventory, "inventory");
      inventory.reOpen();
    }
  }
}