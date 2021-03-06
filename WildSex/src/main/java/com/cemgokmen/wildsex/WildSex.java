package com.cemgokmen.wildsex;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import com.cemgokmen.wildsex.api.WildAnimal;

public class WildSex extends JavaPlugin {

    private WildAnimal wildAnimalHandler;

    private int wildSexTask;
    private long startTime;
    private int interval;
    private boolean mateMode;
    private double chance;
    private double maxAnimalsPerBlock;
    private double maxAnimalsCheckRadius;
    private boolean autoUpdate;
    private boolean removeXP;
    private int maxMateDistance;
    private WildSexTaskListener listener;
    protected Set<Entity> lastMateAnimals;

    @Override
    public void onEnable() {
        String packageName = this.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            final Class<?> clazz = Class.forName("com.cemgokmen.wildsex.wildanimal." + version + ".WildAnimalHandler");
            // Check if we have a WildAnimalHandler class at that location.
            if (WildAnimal.class.isAssignableFrom(clazz)) { // Make sure it actually implements WildAnimal
                this.wildAnimalHandler = (WildAnimal) clazz.getConstructor().newInstance(); // Set our handler
            }
        } catch (final Exception e) {
            e.printStackTrace();
            this.getLogger().severe("This CraftBukkit version is not supported.");
            this.getLogger().info("Check for updates at http://dev.bukkit.org/bukkit-plugins/wildsex/");
            this.setEnabled(false);
            return;
        }

        // Start the actual loading part here.
        this.saveDefaultConfig();
        this.reloadConfig();

        this.interval = this.getConfig().getInt("interval") * 20 * 60;
        this.mateMode = this.getConfig().getBoolean("mateMode");
        this.chance = this.getConfig().getDouble("chance");
        this.maxAnimalsPerBlock = this.getConfig().getDouble("maxAnimalsPerBlock");
        this.maxAnimalsCheckRadius = this.getConfig().getDouble("maxAnimalsCheckRadius");
        this.autoUpdate = this.getConfig().getBoolean("autoUpdate");
        this.removeXP = this.getConfig().getBoolean("removeXP");
        this.maxMateDistance = this.getConfig().getInt("maxMateDistance");
        this.lastMateAnimals = new HashSet<Entity>();

        if (this.autoUpdate) {
            Updater updater = new Updater(this, 85038, this.getFile(), Updater.UpdateType.DEFAULT, true);
            getLogger().log(Level.INFO, "Running automatic updater.");
            Updater.UpdateResult result = updater.getResult();
            switch(result)
            {
                case SUCCESS:
                    getLogger().log(Level.INFO, "Success: The updater found an update, and has readied it to be loaded the next time the server restarts/reloads");
                    break;
                case NO_UPDATE:
                    getLogger().log(Level.INFO, "No Update: The updater did not find an update, and nothing was downloaded.");
                    break;
                case DISABLED:
                    getLogger().log(Level.INFO, "Won't Update: The updater was disabled in its configuration file.");
                    break;
                case FAIL_DOWNLOAD:
                    getLogger().log(Level.INFO, "Download Failed: The updater found an update, but was unable to download it.");
                    break;
                case FAIL_DBO:
                    getLogger().log(Level.INFO, "dev.bukkit.org Failed: For some reason, the updater was unable to contact DBO to download the file.");
                    break;
                case FAIL_NOVERSION:
                    getLogger().log(Level.INFO, "No version found: When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'.");
                    break;
                case FAIL_BADID:
                    getLogger().log(Level.INFO, "Bad id: The id provided by the plugin running the updater was invalid and doesn't exist on DBO.");
                    break;
                case FAIL_APIKEY:
                    getLogger().log(Level.INFO, "Bad API key: The user provided an invalid API key for the updater to use.");
                    break;
                case UPDATE_AVAILABLE:
                    getLogger().log(Level.INFO, "There was an update found, but because you had the UpdateType set to NO_DOWNLOAD, it was not downloaded.");
            }
        }

        if (this.removeXP) {
            this.listener = new WildSexTaskListener(this);
            getServer().getPluginManager().registerEvents(this.listener, this);
        }

        this.wildSexTask = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new WildSexTask(this), 0L, this.interval);
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTask(this.wildSexTask);
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        long numSeconds = (this.interval / 20) - (((System.currentTimeMillis() - this.startTime) % (this.interval * 50)) / 1000);
        sender.sendMessage(numSeconds + " seconds left until next wild sex.");
        return true;
    }

    public void clearMatedAnimals() {
        this.lastMateAnimals.clear();
    }

    public void addMatedAnimal(Entity e) {
        this.lastMateAnimals.add(e);
    }

    public WildAnimal getWildAnimalHandler() {
        return wildAnimalHandler;
    }

    public boolean getMateMode() {
        return mateMode;
    }

    public double getChance() {
        return chance;
    }

    public double getMaxAnimalsPerBlock() {
        return maxAnimalsPerBlock;
    }

    public double getMaxAnimalsCheckRadius() {
        return maxAnimalsCheckRadius;
    }

    public int getMaxMateDistance() {
        return maxMateDistance;
    }
}
