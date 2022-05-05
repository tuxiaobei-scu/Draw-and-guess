package com.tuxiaobei.drawandguess.component;
import com.tuxiaobei.drawandguess.slice.MainAbilitySlice;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.utils.Color;


import java.util.ArrayList;

public class ColorSelectButtons {
    private static final String TAG = MainAbilitySlice.class.getName();
    private ArrayList<Button> color_buttons = new ArrayList<>();
    private Button now_color = null;
    private final Color[] paintColors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.BLACK};
    private Color now_Color = Color.RED;
    public void addButton(Button color_button) {
        color_buttons.add(color_button);
        if (now_color == null) now_color = color_button;
        color_button.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                setColor(color_button);
            }
        });
        color_button.setVisibility(Component.VISIBLE);
    }

    public void setColor(Button now_button) {
        for (Button color_button : color_buttons) {
            color_button.setText("");
        }
        for (int i = 0; i < color_buttons.size(); i++) {
            Button color_button = color_buttons.get(i);
            if (color_button.equals(now_button)) {
                now_button.setText("+");
                now_Color = paintColors[i];
            } else {
                color_button.setText("");
            }
        }
        now_color = now_button;
    }

    public Color getColor() {
        return now_Color;
    }
}
