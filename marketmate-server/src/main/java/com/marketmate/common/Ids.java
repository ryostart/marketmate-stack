package com.marketmate.common;

import java.util.UUID;

public final class Ids {
    private Ids() {
    }

    //HTTPでStringでも安全にUUIDへ。フォーマット不正はIllegalArgumentExceptionのまま400処理に載る
    public static UUID uuid(String s) {
        return UUID.fromString(s);
    }
}
