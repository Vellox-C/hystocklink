package com.hystocklink;

import net.minecraft.client.session.Session;

public class SessionOverride {

    private static volatile Session override = null;

    public static void set(Session session) {
        override = session;
    }

    public static Session get() {
        return override;
    }

    public static void clear() {
        override = null;
    }
}
