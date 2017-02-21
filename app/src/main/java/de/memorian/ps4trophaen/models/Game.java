package de.memorian.ps4trophaen.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Model for a game.
 *
 * @author Tom-Philipp Seifert
 * @since 10.10.2014
 */
public class Game implements Parcelable, Comparable<Game> {

    private String name = "";
    private String xref = "";
    private String infos = "";
    private int platin;
    private int gold;
    private int silver;
    private int bronze;
    private int points;
    private String logoPath = "";
    private List<Trophy> trophies;

    public List<Trophy> getTrophies() {
        return trophies;
    }

    public void setTrophies(List<Trophy> trophies) {
        this.trophies = trophies;
    }

    public String getInfos() {
        return infos;
    }

    public int getPlatin() {
        return platin;
    }

    public int getGold() {
        return gold;
    }

    public int getSilver() {
        return silver;
    }

    public int getBronze() {
        return bronze;
    }

    public int getPoints() {
        return points;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInfos(String infos) {
        this.infos = infos;
    }

    public void setPlatin(int platin) {
        this.platin = platin;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setSilver(int silver) {
        this.silver = silver;
    }

    public void setBronze(int bronze) {
        this.bronze = bronze;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getXref() {
        return xref;
    }

    public void setXref(String xref) {
        this.xref = xref;
    }

    public Game() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(xref);
        dest.writeString(infos);
        dest.writeInt(platin);
        dest.writeInt(gold);
        dest.writeInt(silver);
        dest.writeInt(bronze);
        dest.writeInt(points);
        dest.writeString(logoPath);
        if (trophies == null) {
            trophies = new ArrayList<Trophy>();
        }
        Parcelable[] trohpiesArray = new Parcelable[trophies.size()];
        dest.writeParcelableArray(trophies.toArray(trohpiesArray), PARCELABLE_WRITE_RETURN_VALUE);
    }

    public static final Parcelable.Creator<Game> CREATOR
            = new Parcelable.Creator<Game>() {
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    private Game(Parcel in) {
        setName(in.readString());
        setXref(in.readString());
        setInfos(in.readString());
        setPlatin(in.readInt());
        setGold(in.readInt());
        setSilver(in.readInt());
        setBronze(in.readInt());
        setPoints(in.readInt());
        setLogoPath(in.readString());
        // Android Studio bug workaround
        // http://stackoverflow.com/questions/18505973/android-studio-ambiguous-method-call-getclass
        ClassLoader classLoader = (((Object) this).getClass()).getClassLoader();
        try {
            Trophy[] inTrophiesArray = (Trophy[]) in.readParcelableArray(classLoader);
            setTrophies(Arrays.<Trophy>asList(inTrophiesArray));
        }
        catch (ClassCastException e) {
            setTrophies(new ArrayList<Trophy>());
        }
    }

    @Override
    public int compareTo(Game another) {
        return getName().compareTo(another.getName());
    }
}
