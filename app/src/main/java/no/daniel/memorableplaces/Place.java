package no.daniel.memorableplaces;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Place is a data transfer object used to transfer location data between activities.
 */
public class Place implements Parcelable {
    private final String name;
    private final double latitude;
    private final double longitude;

    /**
     * Constructor, needs all possible Place properties.
     * @param name The name of the place (often street name).
     * @param latitude The latitude of the place.
     * @param longitude The longitude of the place.
     */
    public Place(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructor used by Parcel to restore the Place object.
     * @param parcel The parcel object to read from.
     */
    private Place(Parcel parcel) {
        this.name = parcel.readString();
        this.latitude = parcel.readDouble();
        this.longitude = parcel.readDouble();
    }

    /**
     * Getter for place name.
     * @return the name of the place.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the latitude of the place.
     * @return the latitude of the place.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Getter for the longitude of the place.
     * @return the longitude of the place.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Makes a string of the object's properties (supposed to do)
     * Only returns the name of the place, because I can't be bothered to make
     * my own ArrayAdapter.
     * @return the name of the place.
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
