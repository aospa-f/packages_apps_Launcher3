package com.android.launcher3.icons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import com.android.launcher3.customization.IconDatabase;
import com.android.launcher3.dagger.ApplicationContext;
import com.android.launcher3.dagger.LauncherAppSingleton;
import com.android.launcher3.graphics.ThemeManager;
import com.android.launcher3.icons.pack.IconResolver;
import com.android.launcher3.util.ComponentKey;

import javax.inject.Inject;

@LauncherAppSingleton
public class ThirdPartyIconProvider extends LauncherIconProvider {

    private final Context mContext;
    private final ThemeManager mThemeManager;

    @Inject
    public ThirdPartyIconProvider(
            @ApplicationContext Context context,
            ThemeManager themeManager) {
        super(context, themeManager);
        mContext = context;
        mThemeManager = themeManager;
    }

    public ThirdPartyIconProvider(Context context) {
        this(context, ThemeManager.INSTANCE.get(context));
    }

    @SuppressLint("WrongConstant")
    @Override
    public Drawable getIcon(ComponentInfo info, int iconDpi) {
        ComponentKey key = new ComponentKey(
                info.getComponentName(), UserHandle.getUserHandleForUid(info.applicationInfo.uid));

        IconResolver.DefaultDrawableProvider fallback =
                () -> super.getIcon(info, iconDpi);
        Drawable icon = ThirdPartyIconUtils.getByKey(mContext, key, iconDpi, fallback);

        if (icon == null) {
            return fallback.get();
        }
        icon.setChangingConfigurations(icon.getChangingConfigurations()
                | ThirdPartyIconUtils.CONFIG_HINT_NO_WRAP);
        return icon;
    }

    @Override
    protected ThemeData getThemeDataForPackage(String packageName) {
        if (!IconDatabase.getGlobal(mContext).isEmpty()) {
            return null;
        }
        return super.getThemeDataForPackage(packageName);
    }

    @Override
    public void updateSystemState() {
        super.updateSystemState();
        String pack = IconDatabase.getGlobal(mContext);
        if (!pack.isEmpty()) {
            // A global icon pack overrides every icon, so the transient theme code must not
            // be part of the cache key: it changes on resume and used to invalidate the whole
            // icon cache, flashing default icons when returning home.
            mSystemState = mSystemState.withTheme(
                    "pack:" + pack,
                    mThemeManager.getIconState().isCircle());
        }
    }
}
