package adris.altoclef.baritone.brain;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.baritone.brain.tasks.BrainTask;
import adris.altoclef.baritone.brain.utils.ChatGPT;
import adris.altoclef.baritone.brain.utils.WorldState;
import adris.altoclef.tasks.movement.IdleTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.stupid.BeeMovieTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaritoneBrain {

    // AltoClef Mod
    private final AltoClef mod;

    // Memory store
    public List<MemoryItem> memory;
    // Minecraft world state
    public WorldState worldState;
    // ChatGPT
    public ChatGPT chatGPT;
    private boolean taskInProgress = false;

    public BaritoneBrain(AltoClef mod) {
        this.mod = mod;
        this.memory = new ArrayList<>();
        this.chatGPT = new ChatGPT("");
    }

    public void updateWorldState() {
        this.worldState = new WorldState(mod);
    }

    public Task process(BrainTask task) {
        // Update World State
        updateWorldState();

        // Decay memory
        decayMemory();

        if (!taskInProgress) {
            new Thread(() -> {
                try {
                    taskInProgress = true;  // Mark that a task is in progress
                    var nextTask = chatGPT.generateTask(worldState);
                    task.setDebugState(nextTask);

                    Debug.logMessageBrain(nextTask);

//                    memory.forEach(memoryItem -> Debug.logMessageBrain(String.format("Memory: %s", memoryItem)));

                    mod.runUserTask(Task.fromString(nextTask), () -> {
//                        taskInProgress = false;
//                        mod.runUserTask(task);
                    });

                    var chat = mod.mc().getNetworkHandler();

                    if (chat != null) {
//                        chat.sendChatCommand(nextTask);
                    }

//                    var stream = new ByteArrayInputStream(nextTask.getBytes(StandardCharsets.UTF_8));;
//                    mod.runUserTask(
//                            new BeeMovieTask(UUID.randomUUID().toString(),
//                            worldState.playerLocation,
//                            new InputStreamReader(stream)),
//                            () -> mod.runUserTask(Task.fromString(nextTask),
//                                    () -> task.stop(mod)));
                } catch (Exception e) {
                    task.setDebugState(e.getMessage());
                    taskInProgress = false;  // If there was an error, allow a new task to be generated
                    task.stop(mod);
                }
            }).start();
        }

        // Learn from outcomes
        learnFromOutcomes();

        // Communicate thoughts and decisions
        communicateDecisions();

        // Return Task
        return new IdleTask()   ;
    }

    private void decayMemory() {
        LocalDateTime now = LocalDateTime.now();
        memory.removeIf(memoryItem -> {
            // Remove memories older than one day with low importance and low access frequency
            return memoryItem.getTimestamp().isBefore(now.minusDays(1))
                    && memoryItem.getImportance() < 5
                    && memoryItem.getAccessCount() < 3;
        });
    }

    private void learnFromOutcomes() {
        // Code to incorporate feedback from completed goals
    }

    private void communicateDecisions() {
        // Code to communicate thoughts and decisions
    }

    public WorldState getWorldState() {
        return worldState;
    }

    @Override
    public String toString() {
        return "Brain{" +
                "memory=" + memory +
                ", worldState=" + worldState +
                '}';
    }

    public void onStop(AltoClef mod, Task interruptTask) {
        taskInProgress = false;
    }

    public static class MemoryItem {
        private final String content;
        private final LocalDateTime timestamp;
        private final int importance;
        private int accessCount;

        public MemoryItem(String content, LocalDateTime timestamp, int importance) {
            this.content = content;
            this.timestamp = timestamp;
            this.importance = importance;
            this.accessCount = 0;
        }

        public String getContent() {
            return content;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public int getImportance() {
            return importance;
        }

        public int getAccessCount() {
            return accessCount;
        }

        public void incrementAccessCount() {
            this.accessCount++;
        }

        @Override
        public String toString() {
            return "MemoryItem{" +
                    "content='" + content + '\'' +
                    ", timestamp=" + timestamp +
                    ", importance=" + importance +
                    ", accessCount=" + accessCount +
                    '}';
        }
    }
}
