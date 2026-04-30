package chatinputbox.chatinputboxnew;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
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
import com.google.appinventor.components.runtime.util.MediaUtil;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class ChatInputBox extends AndroidViewComponent {

    private final ComponentContainer container;
    private final LinearLayout root;
    private final LinearLayout titleBar;
    private final ImageView drawerToggleButton;
    private final TextView titleTextView;
    private final LinearLayout screenLayout;
    private final LinearLayout drawer;
    private final ScrollView drawerScroll;
    private final LinearLayout conversationList;
    private final TextView newChatButton;
    private final ScrollView scrollView;
    private final LinearLayout messagesBox;
    private final LinearLayout inputBar;
    private final EditText editText;
    private final ImageView audioButton;
    private final ImageView sendButton;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int backgroundColor = Color.rgb(32, 33, 35);
    private int messageBackgroundColor = Color.rgb(52, 53, 65);
    private int inputBackgroundColor = Color.rgb(64, 65, 79);
    private int borderColor = Color.rgb(86, 88, 105);
    private int textColor = Color.WHITE;
    private int hintColor = Color.rgb(170, 170, 170);
    private int buttonColor = Color.WHITE;
    private int codeBackgroundColor = Color.rgb(20, 20, 20);
    private int drawerBackgroundColor = Color.rgb(32, 33, 35);
    private int titleBarBackgroundColor = Color.rgb(24, 26, 32);

    private int cornerRadiusDp = 24;
    private String hint = "Ask me anything";
    private String titleBarText = "CodeIgnite GPT";
    private int streamSectionDelayMs = 220;
    private String drawerOpenIconPath = "";
    private String drawerCloseIconPath = "";
    private boolean drawerExpanded = true;

    private String sendButtonImagePath = "";
    private String micButtonImagePath = "";
    private boolean lockSendWhileGenerating = true;
    private boolean showSendWhileGenerating = true;
    private boolean isGenerating = false;
    private String sendButtonBusyImagePath = "";

    public ChatInputBox(ComponentContainer container) {
        super(container);
        this.container = container;

        root = new LinearLayout(container.$context());
        root.setOrientation(LinearLayout.VERTICAL);

        titleBar = new LinearLayout(container.$context());
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setGravity(Gravity.CENTER_VERTICAL);
        titleBar.setPadding(dp(12), dp(10), dp(12), dp(10));

        drawerToggleButton = makeImageButton();
        titleTextView = new TextView(container.$context());
        titleTextView.setTextSize(19);
        titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        titleTextView.setPadding(dp(10), 0, 0, 0);

        titleBar.addView(drawerToggleButton, new LinearLayout.LayoutParams(dp(40), dp(40)));
        titleBar.addView(titleTextView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        screenLayout = new LinearLayout(container.$context());
        screenLayout.setOrientation(LinearLayout.HORIZONTAL);

        drawer = new LinearLayout(container.$context());
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setPadding(dp(8), dp(10), dp(8), dp(10));

        newChatButton = new TextView(container.$context());
        newChatButton.setText("+ New chat");
        newChatButton.setTypeface(Typeface.DEFAULT_BOLD);
        newChatButton.setTextSize(14);
        newChatButton.setPadding(dp(12), dp(10), dp(12), dp(10));
        newChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewChatClicked();
            }
        });

        drawer.addView(newChatButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        drawerScroll = new ScrollView(container.$context());
        conversationList = new LinearLayout(container.$context());
        conversationList.setOrientation(LinearLayout.VERTICAL);
        drawerScroll.addView(conversationList);

        drawer.addView(drawerScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        LinearLayout chatPane = new LinearLayout(container.$context());
        chatPane.setOrientation(LinearLayout.VERTICAL);
        chatPane.setPadding(dp(8), dp(8), dp(8), dp(8));

        scrollView = new ScrollView(container.$context());
        messagesBox = new LinearLayout(container.$context());
        messagesBox.setOrientation(LinearLayout.VERTICAL);
        messagesBox.setPadding(dp(4), dp(4), dp(4), dp(10));
        scrollView.addView(messagesBox);

        chatPane.addView(scrollView, new LinearLayout.LayoutParams(
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

        inputBar.addView(editText, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));

        audioButton = makeImageButton();
        sendButton = makeImageButton();

        drawerToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetDrawerExpanded(!drawerExpanded);
            }
        });

        inputBar.addView(audioButton, new LinearLayout.LayoutParams(dp(40), dp(40)));
        inputBar.addView(sendButton, new LinearLayout.LayoutParams(dp(40), dp(40)));

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioClicked();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sendButton.isEnabled()) return;
                if (lockSendWhileGenerating) {
                    isGenerating = true;
                    refreshSendButtonState();
                }
                SendClicked(editText.getText().toString());
            }
        });

        chatPane.addView(inputBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        screenLayout.addView(drawer, new LinearLayout.LayoutParams(getDrawerExpandedWidthPx(), ViewGroup.LayoutParams.MATCH_PARENT));
        screenLayout.addView(chatPane, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        root.addView(titleBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(screenLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        applyStyle();
        showWelcomeMessage();
        Width(ViewGroup.LayoutParams.MATCH_PARENT);
        Height(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public View getView() { return root; }

    @SimpleFunction(description = "Adds the chat UI into an Arrangement component.")
    public void AddToArrangement(AndroidViewComponent arrangement) {
        ViewGroup parent = (ViewGroup) arrangement.getView();
        if (root.getParent() != null) ((ViewGroup) root.getParent()).removeView(root);
        parent.addView(root, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @SimpleFunction(description = "Displays and formats an AI response section by section for more natural streaming.")
    public void DisplayAIMessage(String message) {
        final ArrayList<View> sections = renderMessage(message);
        for (int i = 0; i < sections.size(); i++) {
            final View section = sections.get(i);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    messagesBox.addView(section);
                    scrollToBottom();
                }
            }, (long) i * streamSectionDelayMs);
        }
        if (isGenerating) {
            long doneDelay = Math.max(0, (long) (sections.size() - 1) * streamSectionDelayMs + 60);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isGenerating = false;
                    refreshSendButtonState();
                }
            }, doneDelay);
        }
    }

    @SimpleFunction(description = "Replace drawer conversations with a newline-separated list.")
    public void SetConversations(String newlineSeparatedTitles) {
        conversationList.removeAllViews();
        String[] items = newlineSeparatedTitles.split("\\n");
        for (int i = 0; i < items.length; i++) {
            final String title = items[i].trim();
            if (title.length() == 0) continue;
            TextView row = makeConversationRow(title);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConversationSelected(title);
                }
            });
            conversationList.addView(row);
        }
    }

    @SimpleFunction(description = "Request conversations for a specific TinyDB namespace/tag. Handle event TinyDbConversationsRequested.")
    public void FetchConversationsFromTinyDb(String databaseName) {
        TinyDbConversationsRequested(databaseName);
    }

    @SimpleFunction(description = "Clears all AI messages.")
    public void ClearMessages() { messagesBox.removeAllViews(); }
    @SimpleFunction(description = "Set whether AI is currently generating (controls send button state/visibility).")
    public void SetGenerating(boolean generating) { isGenerating = generating; refreshSendButtonState(); }

    private void showWelcomeMessage() {
        messagesBox.removeAllViews();
        messagesBox.addView(makeText("CodeIgnite GPT", 28, true, false));
        messagesBox.addView(makeText("Ask any Question", 16, false, false));
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
                if (!inCode) { addParagraphIfAny(views, paragraph); inCode = true; code.setLength(0); }
                else { views.add(makeCodeBlock(code.toString())); inCode = false; }
                continue;
            }
            if (inCode) { code.append(line).append("\n"); continue; }

            if (line.startsWith("# ") || line.startsWith("## ") || line.startsWith("### ") || line.startsWith("- ") || line.startsWith("* ") || isMarkdownImage(line) || isImageUrl(line.trim()) || line.trim().length() == 0) {
                addParagraphIfAny(views, paragraph);
                if (line.startsWith("# ")) views.add(makeText(line.substring(2), 24, true, false));
                else if (line.startsWith("## ")) views.add(makeText(line.substring(3), 21, true, false));
                else if (line.startsWith("### ")) views.add(makeText(line.substring(4), 18, true, false));
                else if (line.startsWith("- ") || line.startsWith("* ")) views.add(makeText("• " + line.substring(2), 16, false, false));
                else if (isMarkdownImage(line)) views.add(makeImage(extractMarkdownImageUrl(line)));
                else if (isImageUrl(line.trim())) views.add(makeImage(line.trim()));
                continue;
            }
            paragraph.append(line).append("\n");
        }
        if (inCode) views.add(makeCodeBlock(code.toString()));
        addParagraphIfAny(views, paragraph);
        return views;
    }

    private void addParagraphIfAny(ArrayList<View> views, StringBuilder paragraph) {
        String text = paragraph.toString().trim();
        if (text.length() > 0) { views.add(makeText(text, 16, false, true)); paragraph.setLength(0); }
    }

    private TextView makeText(String text, int size, boolean bold, boolean formatted) {
        TextView tv = new TextView(container.$context());
        tv.setText(formatted ? Html.fromHtml(formatInline(text)) : text);
        tv.setTextColor(textColor);
        tv.setTextSize(size);
        tv.setLineSpacing(dp(3), 1.0f);
        tv.setPadding(dp(14), dp(8), dp(14), dp(8));
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(messageBackgroundColor);
        bg.setCornerRadius(dp(14));
        tv.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(5), 0, dp(5));
        tv.setLayoutParams(lp);
        return tv;
    }

    private TextView makeConversationRow(String text) {
        TextView row = new TextView(container.$context());
        row.setText(text);
        row.setEllipsize(TextUtils.TruncateAt.END);
        row.setSingleLine(true);
        row.setTextSize(13);
        row.setTextColor(textColor);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(52, 53, 65));
        bg.setCornerRadius(dp(10));
        row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), 0, dp(4));
        row.setLayoutParams(lp);
        return row;
    }

    private HorizontalScrollView makeCodeBlock(String codeText) { /* unchanged behavior */
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        hsv.setLayoutParams(lp);
        return hsv;
    }

    private View makeImage(final String imageUrl) { /* unchanged behavior */
        final ImageView imageView = new ImageView(container.$context());
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(0, dp(8), 0, dp(8));
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream input = new URL(imageUrl).openStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    handler.post(new Runnable() { @Override public void run() { imageView.setImageBitmap(bitmap); } });
                } catch (Exception e) {
                    handler.post(new Runnable() { @Override public void run() { messagesBox.addView(makeText("Image could not be loaded: " + imageUrl, 14, false, false)); } });
                }
            }
        }).start();
        return imageView;
    }

    private ImageView makeImageButton() {
        ImageView button = new ImageView(container.$context());
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        button.setPadding(dp(6), dp(6), dp(6), dp(6));
        return button;
    }

    private void setButtonAsset(ImageView view, String path, String fallbackGlyph) {
        try {
            if (path != null && path.trim().length() > 0) {
                Drawable drawable = MediaUtil.getBitmapDrawable(container.$form(), path);
                view.setImageDrawable(drawable);
                view.setBackgroundColor(Color.TRANSPARENT);
                return;
            }
        } catch (Exception ignored) {}
        view.setImageDrawable(null);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(84, 86, 102));
        bg.setShape(GradientDrawable.OVAL);
        view.setBackground(bg);
        view.setContentDescription(fallbackGlyph);
    }

    private String formatInline(String text) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        safe = safe.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        safe = safe.replaceAll("`(.*?)`", "<tt>$1</tt>");
        return safe.replace("\n", "<br>");
    }
    private boolean isMarkdownImage(String line) { return line.trim().startsWith("![") && line.contains("](") && line.endsWith(")"); }
    private String extractMarkdownImageUrl(String line) { int start = line.indexOf("]("); return line.substring(start + 2, line.length() - 1); }
    private boolean isImageUrl(String line) { String lower = line.toLowerCase(); return lower.startsWith("http") && (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif") || lower.endsWith(".webp")); }

    private void applyStyle() {
        root.setBackgroundColor(backgroundColor);
        drawer.setBackgroundColor(drawerBackgroundColor);
        titleBar.setBackgroundColor(titleBarBackgroundColor);
        titleTextView.setText(titleBarText);
        titleTextView.setTextColor(textColor);
        refreshDrawerToggleIcon();
        GradientDrawable newChatBg = new GradientDrawable();
        newChatBg.setColor(Color.rgb(64, 65, 79));
        newChatBg.setCornerRadius(dp(10));
        newChatButton.setBackground(newChatBg);
        newChatButton.setTextColor(textColor);

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(inputBackgroundColor);
        inputBg.setCornerRadius(dp(cornerRadiusDp));
        inputBg.setStroke(dp(1), borderColor);
        inputBar.setBackground(inputBg);
        editText.setHint(hint);
        editText.setTextColor(textColor);
        editText.setHintTextColor(hintColor);

        setButtonAsset(sendButton, sendButtonImagePath, "Send");
        setButtonAsset(audioButton, micButtonImagePath, "Mic");
        refreshSendButtonState();
        updateDrawerLayoutWidth();
    }

    private int getDrawerExpandedWidthPx() {
        return Math.max(dp(180), Math.round(container.$context().getResources().getDisplayMetrics().widthPixels * 0.8f));
    }

    private void refreshDrawerToggleIcon() {
        String fallback = drawerExpanded ? "Collapse" : "Expand";
        String iconPath = drawerExpanded ? drawerCloseIconPath : drawerOpenIconPath;
        setButtonAsset(drawerToggleButton, iconPath, fallback);
    }

    private void updateDrawerLayoutWidth() {
        ViewGroup.LayoutParams lp = drawer.getLayoutParams();
        if (!(lp instanceof LinearLayout.LayoutParams)) return;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lp;
        params.width = drawerExpanded ? getDrawerExpandedWidthPx() : 0;
        drawer.setLayoutParams(params);
        drawer.setVisibility(drawerExpanded ? View.VISIBLE : View.GONE);
        refreshDrawerToggleIcon();
    }

    @SimpleFunction(description = "Opens or collapses the drawer. Open = 80% screen width. Collapsed = hidden.")
    public void SetDrawerExpanded(boolean expanded) {
        drawerExpanded = expanded;
        updateDrawerLayoutWidth();
    }

    @SimpleProperty(description = "Returns true if the drawer is expanded.")
    public boolean DrawerExpanded() { return drawerExpanded; }

    private void refreshSendButtonState() {
        boolean enabled = !(lockSendWhileGenerating && isGenerating);
        sendButton.setEnabled(enabled);
        sendButton.setClickable(enabled);
        sendButton.setAlpha(enabled ? 1.0f : 0.45f);
        sendButton.setVisibility((isGenerating && !showSendWhileGenerating) ? View.GONE : View.VISIBLE);
        if (isGenerating && sendButtonBusyImagePath != null && sendButtonBusyImagePath.trim().length() > 0) {
            setButtonAsset(sendButton, sendButtonBusyImagePath, "Generating");
        } else {
            setButtonAsset(sendButton, sendButtonImagePath, "Send");
        }
    }

    private void scrollToBottom() { scrollView.post(new Runnable() { @Override public void run() { scrollView.fullScroll(View.FOCUS_DOWN); } }); }
    private int dp(int value) { return Math.round(value * container.$context().getResources().getDisplayMetrics().density); }

    @SimpleEvent(description = "Triggered when the send button is clicked. Returns the current prompt text.")
    public void SendClicked(String prompt) { EventDispatcher.dispatchEvent(this, "SendClicked", prompt); }
    @SimpleEvent(description = "Triggered when the audio button is clicked.")
    public void AudioClicked() { EventDispatcher.dispatchEvent(this, "AudioClicked"); }
    @SimpleEvent(description = "Triggered when user picks a conversation in the left drawer.")
    public void ConversationSelected(String title) { EventDispatcher.dispatchEvent(this, "ConversationSelected", title); }
    @SimpleEvent(description = "Triggered when user taps new chat button in the drawer.")
    public void NewChatClicked() { EventDispatcher.dispatchEvent(this, "NewChatClicked"); }
    @SimpleEvent(description = "Use this event to fetch conversation list from TinyDB using given database/tag name.")
    public void TinyDbConversationsRequested(String databaseName) { EventDispatcher.dispatchEvent(this, "TinyDbConversationsRequested", databaseName); }

    @SimpleFunction(description = "Returns the current text inside the input box.")
    public String Text() { return editText.getText().toString(); }
    @SimpleFunction(description = "Sets the text inside the input box.")
    public void SetText(String text) { editText.setText(text); }
    @SimpleFunction(description = "Clears the input box.")
    public void Clear() { editText.setText(""); }

    @SimpleProperty(description = "Sets image asset path for send button. Example: send.png")
    public void SendButtonImage(String value) { sendButtonImagePath = value; applyStyle(); }
    @SimpleProperty(description = "Sets image asset path for send button while AI is generating.")
    public void SendButtonBusyImage(String value) { sendButtonBusyImagePath = value; applyStyle(); }
    @SimpleProperty(description = "Sets image asset path for microphone button. Example: mic.png")
    public void MicButtonImage(String value) { micButtonImagePath = value; applyStyle(); }
    @SimpleProperty(description = "Sets image asset path for title bar icon when drawer is closed (open icon).")
    public void DrawerOpenIconImage(String value) { drawerOpenIconPath = value; applyStyle(); }
    @SimpleProperty(description = "Sets image asset path for title bar icon when drawer is open (collapse icon).")
    public void DrawerCollapseIconImage(String value) { drawerCloseIconPath = value; applyStyle(); }
    @SimpleProperty(description = "Sets the title text displayed in the top title bar.")
    public void TitleBarText(String value) { titleBarText = value; applyStyle(); }
    @SimpleProperty(description = "Returns the current title bar text.")
    public String TitleBarText() { return titleBarText; }
    @SimpleProperty(description = "If true, send button becomes disabled while AI is generating.")
    public void DisableSendWhileGenerating(boolean value) { lockSendWhileGenerating = value; refreshSendButtonState(); }
    @SimpleProperty(description = "If true, send button is shown while AI is generating. If false it is hidden.")
    public void ShowSendWhileGenerating(boolean value) { showSendWhileGenerating = value; refreshSendButtonState(); }
    @SimpleProperty(description = "Sets the hint text shown when the input is empty.")
    public void Hint(String value) { hint = value; applyStyle(); }
    @SimpleProperty(description = "Returns the hint text.")
    public String Hint() { return hint; }
    @SimpleProperty(description = "Sets section streaming delay in milliseconds.")
    public void StreamSectionDelay(int value) { streamSectionDelayMs = value; }
    @SimpleProperty(description = "Sets the dark background color.")
    public void BackgroundColor(int value) { backgroundColor = value; applyStyle(); }
    @SimpleProperty(description = "Sets top title bar background color.")
    public void TitleBarBackgroundColor(int value) { titleBarBackgroundColor = value; applyStyle(); }
    @SimpleProperty(description = "Sets the AI message background color.")
    public void MessageBackgroundColor(int value) { messageBackgroundColor = value; }
    @SimpleProperty(description = "Sets the input bar background color.")
    public void InputBackgroundColor(int value) { inputBackgroundColor = value; applyStyle(); }
    @SimpleProperty(description = "Sets the border color of the input box.")
    public void BorderColor(int value) { borderColor = value; applyStyle(); }
    @SimpleProperty(description = "Sets the text color.")
    public void TextColor(int value) { textColor = value; applyStyle(); }
    @SimpleProperty(description = "Sets the hint text color.")
    public void HintColor(int value) { hintColor = value; applyStyle(); }
    @SimpleProperty(description = "Sets the corner radius in dp.")
    public void CornerRadius(int value) { cornerRadiusDp = value; applyStyle(); }
}
