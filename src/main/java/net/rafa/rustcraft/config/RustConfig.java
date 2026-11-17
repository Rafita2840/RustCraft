package net.rafa.rustcraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.rafa.rustcraft.RustCraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Balance values and switches, stored in {@code config/rustcraft.json}.
 * <p>
 * The file is created with the defaults on first start and rewritten after every load, so options added in
 * later versions appear in it automatically. A broken file is never overwritten: the defaults are used and
 * the error is logged. Values are read on the server (the integrated server in single player);
 * the ones a client needs are sent by {@link ConfigSync}.
 * <p>
 * Per-world on/off switches are game rules instead, see {@link ModGameRules}.
 */
public final class RustConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static RustConfig instance = new RustConfig();

    public Thirst thirst = new Thirst();
    public Gamma gamma = new Gamma();

    public static final class Thirst {
        /** Thirst of a brand new player (0 to 20). */
        public int startThirst = 20;
        /** Thirst after dying (0 to 20). */
        public int respawnThirst = 10;
        /** Every tick adds drain to a hidden counter. Each time it reaches this, thirst drops by 1. */
        public int exhaustionLimit = 5000;
        /** Drain per tick while standing still. */
        public int idleDrain = 1;
        /** Extra drain per tick while walking. */
        public int walkDrain = 2;
        /** Extra drain per tick while running. */
        public int runDrain = 7;
        /** Extra drain per tick while sprinting. */
        public int sprintDrain = 2;
    }

    public static final class Gamma {
        /** Limit how bright players can set the gamma slider. Enforced by P1.3, off until then. */
        public boolean forceLow = false;
        /** Highest gamma allowed while {@code forceLow} is on (0.0 to 1.0). */
        public float maxGamma = 0.5f;
    }

    public static RustConfig get() {
        return instance;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve(RustCraft.MOD_ID + ".json");
    }

    /**
     * Reads the file (creating it if missing).
     *
     * @return false if the file could not be read; the previous values stay active in that case
     */
    public static boolean load() {
        Path path = file();
        RustConfig loaded;
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                loaded = parse(reader);
            } catch (IOException | JsonParseException e) {
                RustCraft.LOGGER.error("Could not read {}, keeping the current values: {}", path, e.toString());
                return false;
            }
        } else {
            loaded = new RustConfig();
        }
        instance = loaded;
        save();
        return true;
    }

    /**
     * Turns file contents into a usable config: missing options keep their defaults and values are clamped.
     * Public so tests can use it without touching files.
     *
     * @throws JsonParseException if the text is not valid JSON
     */
    public static RustConfig parse(Reader reader) {
        RustConfig parsed = GSON.fromJson(reader, RustConfig.class);
        if (parsed == null) {
            parsed = new RustConfig();
        }
        // Sections missing from the file come back as null
        if (parsed.thirst == null) parsed.thirst = new Thirst();
        if (parsed.gamma == null) parsed.gamma = new Gamma();
        parsed.clamp();
        return parsed;
    }

    private static void save() {
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            RustCraft.LOGGER.error("Could not write {}: {}", path, e.toString());
        }
    }

    /** Keeps hand-edited values in a range the game can handle. */
    private void clamp() {
        thirst.startThirst = Math.max(0, Math.min(thirst.startThirst, 20));
        thirst.respawnThirst = Math.max(0, Math.min(thirst.respawnThirst, 20));
        thirst.exhaustionLimit = Math.max(1, thirst.exhaustionLimit);
        thirst.idleDrain = Math.max(0, thirst.idleDrain);
        thirst.walkDrain = Math.max(0, thirst.walkDrain);
        thirst.runDrain = Math.max(0, thirst.runDrain);
        thirst.sprintDrain = Math.max(0, thirst.sprintDrain);
        gamma.maxGamma = Math.max(0f, Math.min(gamma.maxGamma, 1f));
    }
}
