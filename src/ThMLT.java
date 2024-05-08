// ThMLT.java
package com.hridoy.thmlt;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

@DesignerComponent(
        version = 1,
        description = "Extension for theme and translation",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "images/extension.png"
)
@SimpleObject(external = true)
public class ThMLT extends AndroidNonvisibleComponent {

    public static String mFontRegular = "";
    public static String mFontBold = "";
    public static String mFontItalic = "";
    public static String mFontMaterial = "";

    public static int mColorPrimary = 0;
    public static int mColorSecondary = 0;
    public static int mColorAccent = 0;


    public static String mTranslationLanguage = "";

    public Typeface fontTypeface;

    public static HashMap<String, Integer> colorMap = new HashMap<>();
    public static HashMap<String, String> fontMap = new HashMap<>();
    public static HashMap<String, JSONObject> translationMap = new HashMap<>();

    private boolean isRepl = false;
    private final Context context;

    public ThMLT(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        this.isRepl = true;
    }
    //---------------------------------------------------------------------------
    // Properties
    //---------------------------------------------------------------------------
    @SimpleProperty(description = "The regular font style")
    public String FontRegular() {
        return mFontRegular;
    }
    @SimpleProperty(description = "The regular font style")
    public void FontRegular(String fontRegular) {
        mFontRegular = fontRegular;
    }

    @SimpleProperty(description = "The bold font style")
    public String FontBold() {
        return mFontBold;
    }
    @SimpleProperty(description = "The bold font style")
    public void FontBold(String fontBold) {
        mFontBold = fontBold;
    }

    @SimpleProperty(description = "The italic font style")
    public String FontItalic() {
        return mFontItalic;
    }
    @SimpleProperty(description = "The italic font style")
    public void FontItalic(String fontItalic) {
        mFontItalic = fontItalic;
    }

    @SimpleProperty(description = "The material font style")
    public String FontMaterial() {
        return mFontMaterial;
    }
    @SimpleProperty(description = "The material font style")
    public void FontMaterial(String fontMaterial) {
        mFontMaterial = fontMaterial;
    }

    @SimpleProperty(description = "The primary color")
    public int ColorPrimary() {
        return mColorPrimary;
    }
    @SimpleProperty(description = "The primary color")
    public void ColorPrimary(int colorPrimary) {
        mColorPrimary = colorPrimary;
    }

    @SimpleProperty(description = "The secondary color")
    public int ColorSecondary() {
        return mColorSecondary;
    }
    @SimpleProperty(description = "The secondary color")
    public void ColorSecondary(int colorSecondary) {
        mColorSecondary = colorSecondary;
    }

    @SimpleProperty(description = "The accent color")
    public int ColorAccent() {
        return mColorAccent;
    }
    @SimpleProperty(description = "The accent color")
    public void ColorAccent(int colorAccent) {
        mColorAccent = colorAccent;
    }

    //---------------------------------------------------------------------------
    //Events
    //---------------------------------------------------------------------------

    @SimpleEvent(description = "Occurs when an error happens")
    public void ErrorOccurred(String errorFrom, String error) {
        EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorFrom, error);
    }

    //---------------------------------------------------------------------------
    //Methods
    //---------------------------------------------------------------------------
    @SimpleFunction(description = "Initialize the extension\nIf you want bold/italic font to be same font as regular then set value r.")
    public void Initialize(YailDictionary colorScheme, YailDictionary fonts, YailDictionary translationFiles, String defaultLanguage) {
        parseColorScheme(colorScheme);
        parseFonts(fonts);
        parseTranslationFiles(translationFiles);
        mTranslationLanguage = defaultLanguage;
    }

    @SimpleFunction(description = "Update the color scheme")
    public void UpdateColorScheme(YailDictionary colorScheme) {
        updateColorScheme(colorScheme);
    }

    @SimpleFunction(description = "Update the font styles")
    public void UpdateFonts(YailDictionary fonts) {
        updateFonts(fonts);
    }

    @SimpleFunction(description = "Update the translation files")
    public void UpdateTranslationFiles(YailDictionary translationFiles) {
        updateTranslationFiles(translationFiles);
    }

    @SimpleFunction(description = "Translates all the textview")
    public void TranslateApp(AndroidViewComponent layout) {
        ViewGroup mScreenParent = (ViewGroup) layout.getView();
        findTextViews(mScreenParent, mTranslationLanguage);
    }

    @SimpleFunction(description = "Translates all the textview")
    public void TranslateAppForLanguage(AndroidViewComponent layout, String language) {

        ViewGroup mScreenParent = (ViewGroup) layout.getView();
        findTextViews(mScreenParent, language);
    }

    @SimpleFunction(description = "")
    public String GetString(String translationText) {
        if(translationMap.containsKey(mTranslationLanguage)){
            JSONObject jsonObject = translationMap.get(mTranslationLanguage);
            return jsonObject.optString(translationText, "Null");
        }else {
            ErrorOccurred("GetString", "language file not found");
            return "Null";
        }

    }

    @SimpleFunction(description = "")
    public String GetStringForLanguage(String translationText, String language) {
        if(translationMap.containsKey(language)){
            JSONObject jsonObject = translationMap.get(language);
            return jsonObject.optString(translationText, "Null");
        }else {
            ErrorOccurred("GetString", "language file not found");
            return "Null";
        }

    }
    @SimpleFunction(description = "")
    public void UpdateLanguage(String language){
        if (translationMap.containsKey(language)){
            mTranslationLanguage = language;
        }else {
            ErrorOccurred("UpdateLanguage", "Translation file not found");
        }

    }
    @SimpleFunction(description = "")
    public String GetLanguage(){
        return mTranslationLanguage;
    }

    //---------------------------------------------------------------------------
    //Private Methods
    //---------------------------------------------------------------------------



    private void updateColorScheme(YailDictionary colorScheme) {
        for (Object key : colorScheme.keySet()) {
            String colorKey = key.toString().substring(0, 1);
            int parsedColor = parseColor(colorScheme.get(key).toString());
            if (parsedColor == 0) {
                ErrorOccurred("colorScheme", key + ": " + colorScheme.get(key).toString() + " is not a valid color");
                continue; // Skip setting the color if it's not valid
            }
            switch (colorKey) {
                case "p":
                    mColorPrimary = parsedColor;
                    break;
                case "s":
                    mColorSecondary = parsedColor;
                    break;
                case "a":
                    mColorAccent = parsedColor;
                    break;
            }
            if (colorMap.containsKey(colorKey)) {
                colorMap.replace(colorKey, parsedColor);
            } else {
                colorMap.put(colorKey, parsedColor);
            }
        }
    }

    private void updateFonts(YailDictionary fonts) {
        for (Object key : fonts.keySet()) {
            String fontKey = key.toString().substring(0, 1);
            String fontValue = fonts.get(key).toString();
            switch (fontKey) {
                case "r":
                    mFontRegular = fontValue;
                    break;
                case "b":
                    mFontBold = fontValue;
                    break;
                case "i":
                    mFontItalic = fontValue;
                    break;
                case "m":
                    mFontMaterial = fontValue;
                    break;
            }
            if (fontMap.containsKey(fontKey)) {
                fontMap.replace(fontKey, fontValue);
            } else {
                fontMap.put(fontKey, fontValue);
            }
        }
    }

    private void updateTranslationFiles(YailDictionary translationFiles) {
        for (Object key : translationFiles.keySet()) {
            String language = key.toString();
            String jsonString = translationFiles.get(key).toString();
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (translationMap.containsKey(language)) {
                    translationMap.replace(language, jsonObject);
                } else {
                    translationMap.put(language, jsonObject);
                }
            } catch (JSONException e) {
                ErrorOccurred("translationFiles", "Error parsing JSON for language: " + language);
            }
        }
    }

    private void parseColorScheme(YailDictionary colorScheme) {
        for (Object key : colorScheme.keySet()) {
            String colorKey = key.toString().substring(0, 1);
            String colorValue = colorScheme.get(key).toString();
            int parsedColor = parseColor(colorValue);
            if (parsedColor != 0) {
                switch (colorKey) {
                    case "p":
                        mColorPrimary = parsedColor;
                        break;
                    case "s":
                        mColorSecondary = parsedColor;
                        break;
                    case "a":
                        mColorAccent = parsedColor;
                        break;
                }
                colorMap.put(colorKey, parsedColor);
            } else {
                ErrorOccurred("colorScheme", key + ": " + colorValue + " is not a valid color");
            }
        }
    }

    private void parseFonts(YailDictionary fonts) {
        for (Object key : fonts.keySet()) {
            String fontKey = key.toString().substring(0, 1);
            String fontValue = fonts.get(key).toString();
            switch (fontKey) {
                case "r":
                    mFontRegular = fontValue;
                    break;
                case "b":
                    mFontBold = fontValue;
                    break;
                case "i":
                    mFontItalic = fontValue;
                    break;
                case "m":
                    mFontMaterial = fontValue;
                    break;
            }
            fontMap.put(fontKey, fontValue);
        }
    }

    private void parseTranslationFiles(YailDictionary translationFiles) {
        for (Object key : translationFiles.keySet()) {
            String language = key.toString();
            String jsonString = translationFiles.get(key).toString();
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                translationMap.put(language, jsonObject);
            } catch (JSONException e) {
                ErrorOccurred("translationFiles", "Error parsing JSON for language: " + language);
            }
        }
    }

    private int parseColor(String colorValue) {
        try {
            if (colorValue.startsWith("#") && (colorValue.length() == 7 || colorValue.length() == 9)) {
                return android.graphics.Color.parseColor(colorValue);
            } else {
                return Integer.parseInt(colorValue);
            }
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public void findTextViews(View v, String lang) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    // recursively call this method
                    findTextViews(child, lang);
                }
            } else if (v instanceof TextView) {
                //do whatever you want ...
                TextView textView = (TextView) v;
                String text = (String) textView.getText();

                if (text.startsWith("{-")) {
                    String mStrTranslate = text.substring(2, 3); // Get the mStrTranslate letter
                    String mStrFont = text.substring(3, 4); // Get the mStrFont letter
                    String mStrColor = text.substring(4, 5); // Get the mStrColor letter

                    // Condition for the first letter
                    if (mStrTranslate.equals("t")) {
                        textView.setText(GetStringForLanguage(text.substring(6), lang));
                    }else {
                        textView.setText(text.substring(6));
                    }

                    // Condition for the second letter
                    if (fontMap.containsKey(mStrFont)){
                        if (Objects.equals(fontMap.get(mStrFont), "r")){
                            if (mStrFont.equals("b")){
                                CustomFont(fontMap.get("r"));
                                textView.setTypeface(Typeface.create(fontTypeface, Typeface.BOLD));
                            } else if (mStrFont.equals("i")) {
                                CustomFont(fontMap.get("r"));
                                textView.setTypeface(Typeface.create(fontTypeface, Typeface.ITALIC));
                            }
                        }else {
                            CustomFont(fontMap.get(mStrFont));
                            textView.setTypeface(fontTypeface);
                        }
                    }

                    // Condition for the third letter
                    if (colorMap.containsKey(mStrColor)){
                        textView.setTextColor(colorMap.get(mStrColor));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CustomFont(String fontName) {
        if (fontName != null && !fontName.isEmpty()) {
            Typeface tp;
            if (!fontName.contains("/")) {
                if (this.isRepl) {
                    if (Build.VERSION.SDK_INT > 28) {
                        tp = Typeface.createFromFile(new java.io.File("/storage/emulated/0/Android/data/edu.mit.appinventor.aicompanion3/files/assets/".concat(String.valueOf(fontName))));
                    } else {
                        tp = Typeface.createFromFile(new java.io.File("/storage/emulated/0/Android/data/edu.mit.appinventor.aicompanion3/files/AppInventor/assets/".concat(String.valueOf(fontName))));
                    }
                } else {
                    tp = Typeface.createFromAsset(this.context.getAssets(), fontName);
                }
            } else {
                tp = Typeface.createFromFile(new File(fontName));
            }

            if (tp != null) {
                this.fontTypeface = tp;
            }
        }
    }
}
