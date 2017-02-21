package de.memorian.ps4trophaen.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.memorian.ps4trophaen.R;

/**
 * Keys that are used in the database for trophy text.
 *
 * @since 12.10.2014
 */
public class TrophyKeys {

    public static final String BULLET = "|b|";
    public static final String UNDERLINE = "\\[u\\].+:";
    public static final String LINK = "link::.+::.+";
    public static final String X = "|x|";
    public static final String RECTANGLE = "|4|";
    public static final String TRIANGLE = "|3|";
    public static final String CIRCLE = "|o|";
    public static final String L1 = "|l1|";
    public static final String L2 = "|l2|";
    public static final String L3 = "|l3|";
    public static final String R1 = "|r1|";
    public static final String R2 = "|r2|";
    public static final String R3 = "|r3|";
    public static final String PAD = "|pad|";
    public static final String OPTIONS = "|opt|";

    /**
     * The icon patters and resources.
     */
    private static final Map<Pattern, Integer> icons = new HashMap<Pattern, Integer>();
    /**
     * The pattern for the bullet.
     */
    private static final Pattern bulletPattern;
    private static final Pattern linkPattern;
    /**
     * The pattern for underline.
     */
    private static final Pattern underlinePattern;
    private static final Spannable.Factory spannableFactory = Spannable.Factory
            .getInstance();

    static {
        addPattern(X, R.drawable.x_ic);
        addPattern(RECTANGLE, R.drawable.rectangle_ic);
        addPattern(TRIANGLE, R.drawable.triangle_ic);
        addPattern(CIRCLE, R.drawable.o_ic);
        addPattern(L1, R.drawable.l1_ic);
        addPattern(L2, R.drawable.l2_ic);
        addPattern(L3, R.drawable.l3_ic);
        addPattern(R1, R.drawable.r1_ic);
        addPattern(R2, R.drawable.r2_ic);
        addPattern(R3, R.drawable.r3_ic);
        addPattern(PAD, R.drawable.pad_ic);
        addPattern(OPTIONS, R.drawable.opt_ic);
        bulletPattern = Pattern.compile(Pattern.quote(BULLET));
        underlinePattern = Pattern.compile(UNDERLINE);
        linkPattern = Pattern.compile(LINK);
    }

    private static void addPattern(String pattern,
                                   int resource) {
        icons.put(Pattern.compile(Pattern.quote(pattern)), resource);
    }

    private static void addIcons(Context context, SpannableStringBuilder spannable) {
        insertImageSpans(context, spannable);
        insertBulletSpan(spannable);
        insertUnderlineSpan(spannable);
        insertHtmlSpan(spannable);
    }

    private static void insertImageSpans(Context context, SpannableStringBuilder spannable) {
        for (Map.Entry<Pattern, Integer> entry : icons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(),
                        matcher.end(), ImageSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start()
                            && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                if (set) {
                    spannable.setSpan(new ImageSpan(context, entry.getValue()),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    private static void insertHtmlSpan(SpannableStringBuilder spannable) {
        Matcher matcher = linkPattern.matcher(spannable);
        int matchesSoFar = 0;
        while (matcher.find()) {
            boolean set = true;
            for (URLSpan span : spannable.getSpans(matcher.start(),
                    matcher.end(), URLSpan.class))
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end())
                    spannable.removeSpan(span);
                else {
                    set = false;
                    break;
                }
            if (set) {
                String[] parts = matcher.group().split("::");
                if(parts.length == 3) {
                    String url = parts[1];
                    String text = parts[2];
                    int start = matcher.start() - matchesSoFar;
                    int deleteLength = parts[0].length() + parts[1].length() + 4;
                    spannable.delete(start, start + deleteLength);
                    if(url.contains("watch?") && url.contains("v=")) {
                        spannable.setSpan(new YouTubeSpan(url),
                                start, start + text.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else {
                        spannable.setSpan(new URLSpan(url),
                                start, start + text.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    matchesSoFar += deleteLength;
                }
            }
        }
    }

    private static void insertBulletSpan(SpannableStringBuilder spannable) {
        Matcher matcher = bulletPattern.matcher(spannable);
        int matchesSoFar = 0;
        while (matcher.find()) {
            boolean set = true;
            for (BulletSpan span : spannable.getSpans(matcher.start(),
                    matcher.end(), BulletSpan.class))
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end())
                    spannable.removeSpan(span);
                else {
                    set = false;
                    break;
                }
            if (set) {
                int start = matcher.start() - matchesSoFar;
                int end = matcher.end() - matchesSoFar;
                spannable.delete(start, start + 3);
                spannable.setSpan(new BulletSpan(30),
                        start, start + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                matchesSoFar += 3;
            }
        }
    }

    private static void insertUnderlineSpan(SpannableStringBuilder spannable) {
        Matcher matcher = underlinePattern.matcher(spannable);
        int matchesSoFar = 0;
        while (matcher.find()) {
            boolean set = true;
            for (UnderlineSpan span : spannable.getSpans(matcher.start(),
                    matcher.end(), UnderlineSpan.class))
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end())
                    spannable.removeSpan(span);
                else {
                    set = false;
                    break;
                }
            if (set) {
                int start = matcher.start() - matchesSoFar;
                int end = matcher.end() - matchesSoFar;
                spannable.delete(start, start + 3);
                spannable.setSpan(new UnderlineSpan(),
                        start, end - 3,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                matchesSoFar += 3;
            }
        }
    }

    /**
     * Takes the given CharSequence and replaces all occurrences of the defined keys in this class
     * with the corresponding image icons.
     *
     * @param context A Context object.
     * @param text The text for format.
     * @return A Spannable containing ImageSpans instead of a key.
     */
    public static Spannable getIconedText(Context context, CharSequence text) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);//spannableFactory.newSpannable(text);
        addIcons(context, spannable);
        return spannable;
    }
}
