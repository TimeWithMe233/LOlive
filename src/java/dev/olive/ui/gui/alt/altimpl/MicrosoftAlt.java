package dev.olive.ui.gui.alt.altimpl;


import dev.olive.ui.gui.alt.AccountEnum;
import dev.olive.ui.gui.alt.Alt;

public final class MicrosoftAlt extends Alt {
    private final String refreshToken;

    public MicrosoftAlt(String userName, String refreshToken) {
        super(userName, AccountEnum.MICROSOFT);
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
