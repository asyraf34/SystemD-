import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class CustomFont {
    private Font customFont;
    CustomFont(int style, int size) {
        InputStream is = getClass().getResourceAsStream("/04B_03__.ttf");
        try{
            this.customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(style, size);
        }catch(FontFormatException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public Font getCustomFont() {
        return customFont;
    }
}