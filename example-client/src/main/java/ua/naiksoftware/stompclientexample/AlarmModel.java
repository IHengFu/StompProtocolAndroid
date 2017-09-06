package ua.naiksoftware.stompclientexample;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fuheng on 05.09.17.
 */
public class AlarmModel implements Parcelable {

    private String title;
    private String type;
    private Content content;

    public AlarmModel() {
    }

    public AlarmModel(Parcel p) {
        this.title = p.readString();
        this.type = p.readString();

        this.content = p.readParcelable(Content.class.getClassLoader());
    }

    public static final Creator<AlarmModel> CREATOR = new Creator<AlarmModel>() {
        @Override
        public AlarmModel createFromParcel(Parcel in) {
            return new AlarmModel(in);
        }

        @Override
        public AlarmModel[] newArray(int size) {
            return new AlarmModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public boolean isBreakIn() {
        return "breakIn".equals(type);
    }

    public boolean isTowAway() {
        return "towAway".equals(type);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(type);
        parcel.writeParcelable(content,i);
    }
    @Override
    public String toString() {
        return "AlarmModel{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", content=" + content +
                '}';
    }

    public static class Content implements Parcelable {
        private String en;
        private String cn;

        public Content() {
        }

        public Content(Parcel parcel) {
            en = parcel.readString();
            cn = parcel.readString();
        }

        public String getEn() {
            return en;
        }

        public void setEn(String en) {
            this.en = en;
        }

        public String getCn() {
            return cn;
        }

        public void setCn(String cn) {
            this.cn = cn;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "en='" + en + '\'' +
                    ", cn='" + cn + '\'' +
                    '}';
        }

        private static final Creator<Content> CREATOR = new Creator<Content>() {
            @Override
            public Content createFromParcel(Parcel parcel) {

                return new Content(parcel);
            }

            @Override
            public Content[] newArray(int i) {
                return new Content[i];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(en);
            parcel.writeString(cn);
        }
    }

}
