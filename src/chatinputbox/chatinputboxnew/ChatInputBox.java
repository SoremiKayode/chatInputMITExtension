package chatinputbox.chatinputboxnew;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class ChatInputBox extends AndroidViewComponent {

    private final ComponentContainer container;
    private final LinearLayout root;
    private final ScrollView scrollView;
    private final LinearLayout messagesBox;
    private final LinearLayout inputBar;
    private final EditText editText;
    private final TextView audioButton;
    private final TextView sendButton;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int backgroundColor = Color.rgb(33, 33, 33);
    private int messageBackgroundColor = Color.rgb(47, 47, 47);
    private int inputBackgroundColor = Color.rgb(64, 64, 64);
    private int borderColor = Color.rgb(85, 85, 85);
    private int textColor = Color.WHITE;
    private int hintColor = Color.rgb(170, 170, 170);
    private int buttonColor = Color.WHITE;
    private int codeBackgroundColor = Color.rgb(20, 20, 20);

    private int cornerRadiusDp = 24;
    private String hint = "Message ChatGPT";
    private String sendIcon = "Send";
    private String audioIcon = "Mic";
    private int paragraphDelayMs = 450;

    public ChatInputBox(ComponentContainer container) {
        super(container);
        this.container = container;

        root = new LinearLayout(container.$context());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(backgroundColor);
        root.setPadding(dp(8), dp(8), dp(8), dp(8));

        scrollView = new ScrollView(container.$context());
        messagesBox = new LinearLayout(container.$context());
        messagesBox.setOrientation(LinearLayout.VERTICAL);
        messagesBox.setPadding(dp(4), dp(4), dp(4), dp(10));
        scrollView.addView(messagesBox);

        root.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        inputBar = new LinearLayout(container.$context());
        inputBar.setOrientation(LinearLayout.HORIZONTAL);
        inputBar.setGravity(Gravity.CENTER_VERTICAL);
        inputBar.setPadding(dp(12), dp(8), dp(8), dp(8));

        editText = new EditText(container.$context());
        editText.setSingleLine(false);
        editText.setMinLines(1);
        editText.setMaxLines(5);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTextSize(16);
        editText.setPadding(0, 0, dp(8), 0);
        editText.setHint(hint);
        editText.setTextColor(textColor);
        editText.setHintTextColor(hintColor);

        inputBar.addView(editText, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));

        audioButton = makeButton(audioIcon);
        sendButton = makeButton(sendIcon);

        inputBar.addView(audioButton, new LinearLayout.LayoutParams(dp(58), dp(44)));
        inputBar.addView(sendButton, new LinearLayout.LayoutParams(dp(70), dp(44)));

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioClicked();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendClicked(editText.getText().toString());
            }
        });

        root.addView(inputBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        applyStyle();

        Width(ViewGroup.LayoutParams.MATCH_PARENT);
        Height(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public View getView() {
        return root;
    }

    @SimpleFunction(description = "Adds the chat UI into an Arrangement component.")
    public void AddToArrangement(AndroidViewComponent arrangement) {
        ViewGroup parent = (ViewGroup) arrangement.getView();

        if (root.getParent() != null) {
            ((ViewGroup) root.getParent()).removeView(root);
        }

        parent.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    @SimpleFunction(description = "Displays and formats an AI response paragraph by paragraph.")
    public void DisplayAIMessage(String message) {
        final ArrayList<View> views = renderMessage(message);
        for (int i = 0; i < views.size(); i++) {
            final View view = views.get(i);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    messagesBox.addView(view);
                    scrollToBottom();
                }
            }, (long) i * paragraphDelayMs);
        }
    }

    @SimpleFunction(description = "Clears all AI messages.")
    public void ClearMessages() {
        messagesBox.removeAllViews();
    }

    @SimpleFunction(description = "Sets the send button icon or text. Example: Send, Go, >, ➤")
    public void SetSendIcon(String value) {
        sendIcon = value;
        sendButton.setText(value);
    }

    @SimpleFunction(description = "Sets the audio button icon or text. Example: Mic, Audio, Voice, 🎙")
    public void SetAudioIcon(String value) {
        audioIcon = value;
        audioButton.setText(value);
    }

    private ArrayList<View> renderMessage(String message) {
        ArrayList<View> views = new ArrayList<View>();
        String[] lines = message.split("\\n");
        boolean inCode = false;
        StringBuilder code = new StringBuilder();
        StringBuilder paragraph = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.trim().startsWith("```")) {
                if (!inCode) {
                    addParagraphIfAny(views, paragraph);
                    inCode = true;
                    code.setLength(0);
                } else {
                    views.add(makeCodeBlock(code.toString()));
                    inCode = false;
                }
                continue;
            }

            if (inCode) {
                code.append(line).append("\n");
                continue;
            }

            if (line.trim().length() == 0) {
                addParagraphIfAny(views, paragraph);
                continue;
            }

            if (line.startsWith("# ")) {
                addParagraphIfAny(views, paragraph);
                views.add(makeText(line.substring(2), 24, true, false));
            } else if (line.startsWith("## ")) {
                addParagraphIfAny(views, paragraph);
                views.add(makeText(line.substring(3), 21, true, false));
            } else if (line.startsWith("### ")) {
                addParagraphIfAny(views, paragraph);
                views.add(makeText(line.substring(4), 18, true, false));
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                addParagraphIfAny(views, paragraph);
                views.add(makeText("• " + line.substring(2), 16, false, false));
            } else if (isMarkdownImage(line)) {
                addParagraphIfAny(views, paragraph);
                views.add(makeImage(extractMarkdownImageUrl(line)));
            } else if (isImageUrl(line.trim())) {
                addParagraphIfAny(views, paragraph);
                views.add(makeImage(line.trim()));
            } else {
                paragraph.append(line).append("\n");
            }
        }

        if (inCode) {
            views.add(makeCodeBlock(code.toString()));
        }

        addParagraphIfAny(views, paragraph);
        return views;
    }

    private void addParagraphIfAny(ArrayList<View> views, StringBuilder paragraph) {
        String text = paragraph.toString().trim();
        if (text.length() > 0) {
            views.add(makeText(text, 16, false, true));
            paragraph.setLength(0);
        }
    }

    private TextView makeText(String text, int size, boolean bold, boolean formatted) {
        TextView tv = new TextView(container.$context());
        if (formatted) {
            tv.setText(Html.fromHtml(formatInline(text)));
        } else {
            tv.setText(text);
        }
        tv.setTextColor(textColor);
        tv.setTextSize(size);
        tv.setLineSpacing(dp(3), 1.0f);
        tv.setPadding(dp(14), dp(8), dp(14), dp(8));
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(messageBackgroundColor);
        bg.setCornerRadius(dp(14));
        tv.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, dp(5), 0, dp(5));
        tv.setLayoutParams(lp);
        return tv;
    }

    private HorizontalScrollView makeCodeBlock(String codeText) {
        HorizontalScrollView hsv = new HorizontalScrollView(container.$context());

        TextView codeView = new TextView(container.$context());
        codeView.setText(codeText);
        codeView.setTextColor(Color.rgb(220, 220, 220));
        codeView.setTextSize(14);
        codeView.setTypeface(Typeface.MONOSPACE);
        codeView.setPadding(dp(12), dp(12), dp(12), dp(12));
        codeView.setTextIsSelectable(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(codeBackgroundColor);
        bg.setCornerRadius(dp(12));
        codeView.setBackground(bg);

        hsv.addView(codeView);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, dp(6), 0, dp(6));
        hsv.setLayoutParams(lp);

        return hsv;
    }

    private View makeImage(final String imageUrl) {
        final ImageView imageView = new ImageView(container.$context());
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(0, dp(8), 0, dp(8));

        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream input = new URL(imageUrl).openStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView error = makeText("Image could not be loaded: " + imageUrl, 14, false, false);
                            messagesBox.addView(error);
                        }
                    });
                }
            }
        }).start();

        return imageView;
    }

    private String formatInline(String text) {
        String safe = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        safe = safe.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        safe = safe.replaceAll("`(.*?)`", "<tt>$1</tt>");
        safe = safe.replace("\n", "<br>");
        return safe;
    }

    private boolean isMarkdownImage(String line) {
        return line.trim().startsWith("![") && line.contains("](") && line.endsWith(")");
    }

    private String extractMarkdownImageUrl(String line) {
        int start = line.indexOf("](");
        return line.substring(start + 2, line.length() - 1);
    }

    private boolean isImageUrl(String line) {
        String lower = line.toLowerCase();
        return lower.startsWith("http") &&
                (lower.endsWith(".png") ||
                 lower.endsWith(".jpg") ||
                 lower.endsWith(".jpeg") ||
                 lower.endsWith(".gif") ||
                 lower.endsWith(".webp"));
    }

    private TextView makeButton(String text) {
        TextView button = new TextView(container.$context());
        button.setText(text);
        button.setTextColor(buttonColor);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);
        button.setContentDescription(text);
        button.setPadding(dp(6), 0, dp(6), 0);
        return button;
    }

    private void applyStyle() {
        root.setBackgroundColor(backgroundColor);

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(inputBackgroundColor);
        inputBg.setCornerRadius(dp(cornerRadiusDp));
        inputBg.setStroke(dp(1), borderColor);
        inputBar.setBackground(inputBg);

        editText.setHint(hint);
        editText.setTextColor(textColor);
        editText.setHintTextColor(hintColor);

        audioButton.setTextColor(buttonColor);
        sendButton.setTextColor(buttonColor);
    }

    private void scrollToBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private int dp(int value) {
        float density = container.$context().getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    @SimpleEvent(description = "Triggered when the send button is clicked. Returns the current prompt text.")
    public void SendClicked(String prompt) {
        EventDispatcher.dispatchEvent(this, "SendClicked", prompt);
    }

    @SimpleEvent(description = "Triggered when the audio button is clicked.")
    public void AudioClicked() {
        EventDispatcher.dispatchEvent(this, "AudioClicked");
    }

    @SimpleFunction(description = "Returns the current text inside the input box.")
    public String Text() {
        return editText.getText().toString();
    }

    @SimpleFunction(description = "Sets the text inside the input box.")
    public void SetText(String text) {
        editText.setText(text);
    }

    @SimpleFunction(description = "Clears the input box.")
    public void Clear() {
        editText.setText("");
    }

    @SimpleProperty(description = "Sets the hint text shown when the input is empty.")
    public void Hint(String value) {
        hint = value;
        applyStyle();
    }

    @SimpleProperty(description = "Returns the hint text.")
    public String Hint() {
        return hint;
    }

    @SimpleProperty(description = "Sets the paragraph display delay in milliseconds.")
    public void ParagraphDelay(int value) {
        paragraphDelayMs = value;
    }

    @SimpleProperty(description = "Sets the dark background color.")
    public void BackgroundColor(int value) {
        backgroundColor = value;
        applyStyle();
    }

    @SimpleProperty(description = "Sets the AI message background color.")
    public void MessageBackgroundColor(int value) {
        messageBackgroundColor = value;
    }

    @SimpleProperty(description = "Sets the input bar background color.")
    public void InputBackgroundColor(int value) {
        inputBackgroundColor = value;
        applyStyle();
    }

    @SimpleProperty(description = "Sets the border color of the input box.")
    public void BorderColor(int value) {
        borderColor = value;
        applyStyle();
    }

    @SimpleProperty(description = "Sets the text color.")
    public void TextColor(int value) {
        textColor = value;
        applyStyle();
    }

    @SimpleProperty(description = "Sets the hint text color.")
    public void HintColor(int value) {
        hintColor = value;
        applyStyle();
    }

    @SimpleProperty(description = "Sets the button text color.")
    public void ButtonColor(int value) {
        buttonColor = value;
        applyStyle();
    }

    @SimpleProperty(description = "Sets the corner radius in dp.")
    public void CornerRadius(int value) {
        cornerRadiusDp = value;
        applyStyle();
    }
}