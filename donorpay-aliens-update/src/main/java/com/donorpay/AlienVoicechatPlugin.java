package com.donorpay;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;

/** Simple Voice Chat hook: opens the aliens' mouths and gives them alien voices. Only loaded if Simple Voice Chat is installed. */
public class AlienVoicechatPlugin implements VoicechatPlugin {

    private static double phase = 0;
    private static java.lang.reflect.Method idMethod;
    private static boolean idLookedUp;

    private static java.util.UUID senderId(ClientReceiveSoundEvent.EntitySound event) {
        try {
            if (!idLookedUp) {
                idLookedUp = true;
                for (String n : new String[]{"getEntityId", "getId"}) {
                    try { idMethod = ClientReceiveSoundEvent.EntitySound.class.getMethod(n); break; }
                    catch (NoSuchMethodException ignored) { }
                }
                if (idMethod == null) DonorPayClient.LOG.warn("[donorpay] could not find the sender id method in the voice chat API");
            }
            if (idMethod != null) {
                Object o = idMethod.invoke(event);
                if (o instanceof java.util.UUID u) return u;
            }
        } catch (Exception ignored) { }
        return null;
    }

    @Override
    public String getPluginId() { return "donorpay_aliens"; }

    @Override
    public void initialize(VoicechatApi api) { }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::onEntitySound);
        registration.registerEvent(ClientSoundEvent.class, this::onMicrophone);
    }

    private void onEntitySound(ClientReceiveSoundEvent.EntitySound event) {
        AlienMode.markTalking(senderId(event));
        if (AlienMode.on && AlienMode.ALIEN_VOICE) {
            short[] raw = event.getRawAudio();
            if (raw != null && raw.length > 0) {
                short[] copy = raw.clone();
                alienize(copy);
                event.setRawAudio(copy);
            }
        }
    }

    private void onMicrophone(ClientSoundEvent event) {
        AlienMode.markMyTalking(); // this event only fires while you are transmitting
        if (AlienMode.on && AlienMode.ALIEN_MY_MIC) {
            short[] raw = event.getRawAudio();
            if (raw != null && raw.length > 0) {
                short[] copy = raw.clone();
                alienize(copy);
                event.setRawAudio(copy);
            }
        }
    }

    /** Ring-modulation + warble: keeps the audio length, sounds robotic/alien. */
    static void alienize(short[] a) {
        double inc = 2 * Math.PI * 85.0 / 48000.0;
        for (int i = 0; i < a.length; i++) {
            double carrier = Math.sin(phase);
            phase += inc;
            if (phase > Math.PI * 2) phase -= Math.PI * 2;
            double warble = 0.75 + 0.25 * Math.sin(phase * 0.21);
            double out = a[i] * 0.30 + a[i] * carrier * 1.15 * warble;
            if (out > 32767) out = 32767;
            if (out < -32768) out = -32768;
            a[i] = (short) out;
        }
    }
}
