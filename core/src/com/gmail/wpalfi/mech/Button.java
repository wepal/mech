package com.gmail.wpalfi.mech;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by wpalfi on 19.02.17.
 */

public class Button {
    private Texture texture, textureChecked;
    private SpriteBatch batch;

    public Button(String filename, String filenameChecked) {
        texture = new Texture(Gdx.files.internal(filename));
        textureChecked = new Texture(Gdx.files.internal(filenameChecked));
        batch = new SpriteBatch();
    }

    public void render(float y, float w, boolean checked) {
        batch.begin();
        Texture tex= checked ? textureChecked : texture;
        batch.draw(tex, 0, y, w, w);
        batch.end();
    }
}
