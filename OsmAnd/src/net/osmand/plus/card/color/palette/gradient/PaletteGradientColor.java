package net.osmand.plus.card.color.palette.gradient;

import static net.osmand.plus.helpers.ColorPaletteHelper.GRADIENT_ID_SPLITTER;

import androidx.annotation.NonNull;

import net.osmand.ColorPalette;
import net.osmand.plus.card.color.palette.main.data.PaletteColor;

import org.json.JSONException;
import org.json.JSONObject;

public class PaletteGradientColor extends PaletteColor {

    public static String DEFAULT_NAME = "default";

    private ColorPalette colorPalette;

    public PaletteGradientColor(@NonNull String id, @NonNull ColorPalette colorPalette, long creationTime) {
        super(id, 0, creationTime);
        this.colorPalette = colorPalette;
    }

    public PaletteGradientColor(@NonNull String id, @NonNull ColorPalette colorPalette, long creationTime, long lastUsedTime) {
        super(id, 0, creationTime);
        this.colorPalette = colorPalette;
        this.lastUsedTime = lastUsedTime;
    }

    public PaletteGradientColor(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject.getString(ID), 0, 0);
        if (jsonObject.has(CREATION_TIME)) {
            creationTime = jsonObject.getLong(CREATION_TIME);
        } else {
            creationTime = 0;
        }
        if (jsonObject.has(LAST_USED_TIME)) {
            lastUsedTime = jsonObject.getLong(LAST_USED_TIME);
        }
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ID, id);
        if (creationTime > 0) {
            jsonObject.put(CREATION_TIME, creationTime);
        }
        if (lastUsedTime > 0) {
            jsonObject.put(LAST_USED_TIME, lastUsedTime);
        }
        return jsonObject;
    }

    @NonNull
    public String getPaletteName() {
        String[] splitId = getId().split(GRADIENT_ID_SPLITTER);
        return splitId[1];
    }

    @NonNull
    public String getColorizationTypeName(){
        String[] splitId = getId().split(GRADIENT_ID_SPLITTER);
        return splitId[0].toLowerCase();
    }

    @NonNull
    public ColorPalette getColorPalette() {
        return colorPalette;
    }
}
