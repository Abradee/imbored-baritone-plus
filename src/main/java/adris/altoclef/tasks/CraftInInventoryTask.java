package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Item;

import java.util.Optional;

public class CraftInInventoryTask extends ResourceTask {

    private CraftingRecipe _recipe;

    public CraftInInventoryTask(ItemTarget target, CraftingRecipe recipe) {
        super(target);
        _recipe = recipe;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {

    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        if (!mod.getInventoryTracker().hasRecipeMaterials(_recipe)) {
            // Collect recipe materials
            return collectRecipeSubTask(mod);
        }

        craftInstant(mod, _recipe);

        return null;
    }

    private void craftInstant(AltoClef mod, CraftingRecipe recipe) {
        mod.getInventoryTracker().craftInstant(recipe);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CraftInInventoryTask) {
            CraftInInventoryTask t = (CraftInInventoryTask) other;
            if (!t._recipe.equals(_recipe)) return false;
            return isCraftingEqual(t);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return toCraftingDebugStringName() + " " + _recipe;
    }

    // virtual
    protected Task collectRecipeSubTask(AltoClef mod) {
        // Default, just go through the recipe slots and collect the first one.
        for (int i = 0; i < _recipe.getSlotCount(); ++i) {
            CraftingRecipe.CraftingSlot slot = _recipe.getSlot(i);
            if (slot.getTargetItems().size() > 1) {
                Debug.logWarning("Recipe collection for recipe " + _recipe + " slot " + i
                        + " has multiple options, picking the first. Please define an explicit"
                        + " collectRecipeSubTask() function for this task."
                );
            }
            Optional<Item> item = slot.getTargetItems().stream().findFirst();
            if (item.isPresent()) {
                item.get();
            }
        }

        return null;
    }

    protected String toCraftingDebugStringName() {
        return "Craft Task";
    }
    protected boolean isCraftingEqual(CraftInInventoryTask other) {
        return true;
    }
}