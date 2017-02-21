package de.memorian.showcaseview.drawing;

import de.memorian.showcaseview.ShowcaseView;

import android.content.Context;
import android.graphics.Canvas;

/**
 * Created by curraa01 on 13/10/2013.
 */
public interface TextDrawer {

    void draw(Canvas canvas, boolean hasPositionChanged);

    void setDetails(CharSequence details);

    void setTitle(CharSequence title);

    void calculateTextPosition(int canvasW, int canvasH, boolean hasNoTarget, ShowcaseView showcaseView);

    void setTitleStyling(Context context, int styleId);

    void setDetailStyling(Context context, int styleId);
}
