Reactive-inventory is an opinionated and easy-to-use inventory library
for spigot plugins to optimize and improve the workflow with inventories. Thereby
the library offers extendability to serve your own personal demands.

## Documentation

<hr>

### Inventories

This is a standard inventory, which has all its logic in a class. This is the key
design principle of [reactive-inventory](https://github.com/cderszteler/reactive-inventory).

```java
public final class YourInventory implements Inventory {
  @Override
  public void interact(InventoryClickEvent click, Player target, UUID userId) {
    // Code here gets executed, when the player interacts with the inventory
    // (only valid event interactions will trigger this method)
  }

  @Override
  public void open(Player target) {
    // Write here your code to open the player's inventory
  }
}
```

To open an inventory, you inject the `OpenInventoryFactory` via Guice:

```java
OpenInventoryFactory factory;

factory
  .withUserId(UUID.randomUUID()) // The uuid of the inventory owner (needs to be a player)
  .withInventory(inventory) // Can be replaced with a class name to inject an inventory
  .open() // Can be opened synchronized, too

```

#### Policy restricted

Inventories can have (custom) policies, which get applied/checked before every
interaction. A typical policy is the `MoveItemsPolicy`, which can allow or deny
the player to move items in the inventory.

"Policy restricted inventories" can be implemented by add its interface
to your inventory:
```java

public final class YourInventory implements Inventory, PolicyRestrictedInventory {
  // ... your normal inventory code

  @Override
  public Set<InventoryPolicy> policies() {
    return Sets.newHashSet(MoveItemsPolicy.Deny); // Return your set of policies for your custom inventory
  }
}

```

Own policies can easily be implemented, too. A simple example would look as following:

```java
// Implement the InventoryPolicy for your own policy
public enum MoveItemsPolicy implements InventoryPolicy {
  Allow,
  Deny;

  // In this method you can apply all your actions to the event that is then passed to the inventory
  @Override
  public void apply(InventoryClickEvent click) {
    switch (this) {
      case Allow -> click.setCancelled(false);
      case Deny -> click.setCancelled(true);
    }
  }
}
```

#### Reactive updates

Inventories of a certain type can be updated by executing a certain action
(more about this later on). This is helpful e.g. for inventories that show a
poll and update the percentages live.
After an update trigger, the `openUpdated` method of
all inventories of a certain type is executed. By default, this method calls the
normal `open` method, however you may want to cache your results, in which case
you can override this method:

```java
public final class YourInventory implements Inventory, ReactiveInventory {
  // ... your normal inventory code

  @Override
  public void openUpdated(Player target) {
    // Open inventory with cached results
  }
}
```

<br>

### Actions

A fundamental principle of [reactive-inventory](https://github.com/cderszteler/reactive-inventory)
is that you can't access the inventory directly while it is open (except for closing it).
Therefore, you have actions, which can perform default operations like re-opening an inventory or
do as complex things as you like.

Default actions like `ReOpenAction` or `TriggerUpdateAction` are available through
their respective classes or injectable as well.
All actions are performed in the, as well injectable, `OpenInventoryRepository` class.
You can execute actions as following:


```java
OpenInventoryRepostiory repository;

// This will close all open inventories of the type 'YourInventory'
repository.performActionOnType(YourInventory.class, ReOpenAction.lazy());

// This will update the user's inventory
repository.performActionForUser(TriggerUpdateAction.lazy(), userId);
```

You can even create your own Action; structure presented by a simple update action:

```java
// Create your action class by implementing InventoryAction
public final class TriggerUpdateAction implements InventoryAction {
  @Inject
  private TriggerUpdateAction() {}

  @Override
  public ExecutableAction asExecutable() {
    return new InteractExecutable();
  }

  // All actions have an inner class, in which the execution is performed later on.
  // With that design, complex or resource intensive operations can be executed
  //   in the actual action and just passed to the executable
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class InteractExecutable implements ExecutableAction {
    private OpenInventory inventory;

    @Override
    public ExecutableAction withTarget(OpenInventory inventory) {
      this.inventory = inventory;
      return this;
    }

    // The actual operation that gets executed individually for every open inventory
    @Override
    public void perform() {
      Preconditions.checkNotNull(inventory, "inventory");
      inventory.update();
    }
  }
}
```

<hr>

## Download

### Prerequisites

- Java 17
- Spigot 1.12 (should work with higher versions)
- Guice
- Components package (https://github.com/cderszteler/components)
  _Otherwise you can register the listener and add the module to your injector by yourself_


### Via Gradle:

```groovy
  maven {
    url 'https://repo.qetz.de/artifactory/repo-public'
  }
```

```groovy
  implementation 'qetz:reactive-inventory:2.0.3'
```

### Via Maven:

```xml
<repository>
  <id>qetz</id>
  <url>https://repo.qetz.de/artifactory/repo-public</url>
</repository>
```

```xml
<dependency>
  <groupId>qetz</groupId>
  <artifactId>reactive-inventory</artifactId>
  <version>2.0.3</version>
</dependency>
```

<hr>

## Miscellaneous

### Without [components](https://github.com/cderszteler/components) library

If you choose to not use the
[components](https://github.com/cderszteler/components) library,
you must register the following listener `RemoveOpenedInventoryTrigger` and
`InventoryInteractTrigger` as well as add this module `OpenInventoryModule`
to your Guice injector.
