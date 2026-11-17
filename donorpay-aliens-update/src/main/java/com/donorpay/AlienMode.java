package com.donorpay;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Shared state for Alien Mode (press O). */
public final class AlienMode {
    private AlienMode() { }

    public static volatile boolean on = false;
    public static volatile long toastAt = -100000;

    /** Robot/alien voice effect on voices you hear (only while alien mode is on). */
    public static final boolean ALIEN_VOICE = true;
    /** Also change YOUR voice before it is sent, so everyone hears you as an alien (they hear it even without the mod). */
    public static final boolean ALIEN_MY_MIC = false;
    /** How long (ms) the mouth stays open after a voice packet. */
    static final long TALK_MS = 280;

    static final Map<UUID, Long> talking = new ConcurrentHashMap<>();
    public static final Identifier IDLE = Identifier.fromNamespaceAndPath("donorpay", "textures/entity/alien.png");
    public static final Identifier TALK = Identifier.fromNamespaceAndPath("donorpay", "textures/entity/alien_talk.png");

    public static void toggle() {
        on = !on;
        toastAt = System.currentTimeMillis();
    }

    public static void markTalking(UUID id) {
        if (id != null) talking.put(id, System.currentTimeMillis());
    }

    public static void markMyTalking() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) markTalking(mc.player.getUUID());
    }

    static boolean isTalking(UUID id) {
        Long t = talking.get(id);
        return t != null && System.currentTimeMillis() - t < TALK_MS;
    }

    /** Called by the renderer mixin. Returns the alien texture, or null to leave the normal skin. */
    public static Identifier textureFor(int entityId) {
        if (!on) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        Entity e = mc.level.getEntity(entityId);
        if (e == null) return IDLE;
        return isTalking(e.getUUID()) ? TALK : IDLE;
    }
}
