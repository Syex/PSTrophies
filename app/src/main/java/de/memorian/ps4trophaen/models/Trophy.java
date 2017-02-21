package de.memorian.ps4trophaen.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;

/**
 * Model for a trophy.
 *
 * @author Tom-Philipp Seifert
 * @since 11.10.2014
 */
public class Trophy implements Parcelable, Comparable<Trophy> {

    private Type type;
    private String title;
    private String text;
    private String guide;
    private boolean secret;
    private Spannable formattedGuide;
    private String base64Icon;
    private Priority priority;

    public Trophy() {
        priority = Priority.NORMAL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type.name());
        dest.writeString(title);
        dest.writeString(text);
        dest.writeString(guide);
    }

    public static final Parcelable.Creator<Trophy> CREATOR
            = new Parcelable.Creator<Trophy>() {
        public Trophy createFromParcel(Parcel in) {
            return new Trophy(in);
        }

        public Trophy[] newArray(int size) {
            return new Trophy[size];
        }
    };

    private Trophy(Parcel in) {
        setType(Type.valueOf(in.readString()));
        setTitle(in.readString());
        setText(in.readString());
        setGuide(in.readString());
    }

    public String getBase64Icon() {
        return base64Icon;
    }

    public void setBase64Icon(String base64Icon) {
        this.base64Icon = base64Icon;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setFormattedGuide(Spannable spannable) {
        formattedGuide = spannable;
    }

    public Spannable getFormattedGuide(Context context) {
        return formattedGuide;
    }

    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGuide() {
        return guide;
    }

    public void setGuide(String guide) {
        this.guide = guide;
    }

    public Type getType() {

        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public int compareTo(Trophy other) {
        Priority otherPriority = other.getPriority();
        if (getPriority() == Priority.IMPORTANT && otherPriority != Priority.IMPORTANT) {
            return -1;
        }
        if (getPriority() == Priority.NORMAL) {
            if (otherPriority == Priority.IMPORTANT) {
                return 1;
            }
            if (otherPriority == Priority.UNIMPORTANT) {
                return -1;
            }
        }
        if (getPriority() == Priority.UNIMPORTANT && otherPriority != Priority.UNIMPORTANT) {
            return 1;
        }

        Type otherType = other.getType();
        if (getType() == Type.BRONZE) {
            if (otherType == Type.SILVER || otherType == Type.GOLD || otherType == Type.PLATIN) {
                return -1;
            } else return getTitle().compareTo(other.getTitle());
        }

        if (getType() == Type.SILVER) {
            if (otherType == Type.GOLD || otherType == Type.PLATIN) {
                return -1;
            } else if (otherType == Type.BRONZE) {
                return 1;
            } else return getTitle().compareTo(other.getTitle());
        }

        if (getType() == Type.GOLD) {
            if (otherType == Type.PLATIN) {
                return -1;
            } else if (otherType == Type.BRONZE || otherType == Type.SILVER) {
                return 1;
            } else return getTitle().compareTo(other.getTitle());
        }

        if (getType() == Type.PLATIN) {
            if (otherType == Type.PLATIN) {
                return getTitle().compareTo(other.getTitle());
            } else return 1;
        }

        return 0;
    }

    public enum Type {
        BRONZE, SILVER, GOLD, PLATIN;
    }

    public enum Priority {
        NORMAL, IMPORTANT, UNIMPORTANT;
    }
}
