// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: E:\workspace_android\workspace_androidstudio\RocketSinghPillion\protos\request.proto
package request;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import java.util.Collections;
import java.util.List;

import static com.squareup.wire.Message.Datatype.DOUBLE;
import static com.squareup.wire.Message.Datatype.INT64;
import static com.squareup.wire.Message.Datatype.STRING;
import static com.squareup.wire.Message.Label.REPEATED;

public final class TripData extends Message {
  private static final long serialVersionUID = 0L;

  public static final Double DEFAULT_START_LATITUDE = 0D;
  public static final Double DEFAULT_START_LONGITUDE = 0D;
  public static final List<Double> DEFAULT_ON_WAY_LATITUDE = Collections.emptyList();
  public static final List<Double> DEFAULT_ON_WAY_LONGITUDE = Collections.emptyList();
  public static final Double DEFAULT_END_LATITUDE = 0D;
  public static final Double DEFAULT_END_LONGITUDE = 0D;
  public static final Double DEFAULT_TRIP_DISTANCE = 0D;
  public static final Long DEFAULT_TRIP_TIME = 0L;
  public static final String DEFAULT_START_LOCATION = "";

  @ProtoField(tag = 1, type = DOUBLE)
  public final Double start_latitude;

  @ProtoField(tag = 2, type = DOUBLE)
  public final Double start_longitude;

  @ProtoField(tag = 3, type = DOUBLE, label = REPEATED)
  public final List<Double> on_way_latitude;

  @ProtoField(tag = 4, type = DOUBLE, label = REPEATED)
  public final List<Double> on_way_longitude;

  @ProtoField(tag = 5, type = DOUBLE)
  public final Double end_latitude;

  @ProtoField(tag = 6, type = DOUBLE)
  public final Double end_longitude;

  @ProtoField(tag = 7, type = DOUBLE)
  public final Double trip_distance;

  @ProtoField(tag = 8, type = INT64)
  public final Long trip_time;

  @ProtoField(tag = 9, type = STRING)
  public final String start_location;

  public TripData(Double start_latitude, Double start_longitude, List<Double> on_way_latitude, List<Double> on_way_longitude, Double end_latitude, Double end_longitude, Double trip_distance, Long trip_time, String start_location) {
    this.start_latitude = start_latitude;
    this.start_longitude = start_longitude;
    this.on_way_latitude = immutableCopyOf(on_way_latitude);
    this.on_way_longitude = immutableCopyOf(on_way_longitude);
    this.end_latitude = end_latitude;
    this.end_longitude = end_longitude;
    this.trip_distance = trip_distance;
    this.trip_time = trip_time;
    this.start_location = start_location;
  }

  private TripData(Builder builder) {
    this(builder.start_latitude, builder.start_longitude, builder.on_way_latitude, builder.on_way_longitude, builder.end_latitude, builder.end_longitude, builder.trip_distance, builder.trip_time, builder.start_location);
    setBuilder(builder);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof TripData)) return false;
    TripData o = (TripData) other;
    return equals(start_latitude, o.start_latitude)
        && equals(start_longitude, o.start_longitude)
        && equals(on_way_latitude, o.on_way_latitude)
        && equals(on_way_longitude, o.on_way_longitude)
        && equals(end_latitude, o.end_latitude)
        && equals(end_longitude, o.end_longitude)
        && equals(trip_distance, o.trip_distance)
        && equals(trip_time, o.trip_time)
        && equals(start_location, o.start_location);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = start_latitude != null ? start_latitude.hashCode() : 0;
      result = result * 37 + (start_longitude != null ? start_longitude.hashCode() : 0);
      result = result * 37 + (on_way_latitude != null ? on_way_latitude.hashCode() : 1);
      result = result * 37 + (on_way_longitude != null ? on_way_longitude.hashCode() : 1);
      result = result * 37 + (end_latitude != null ? end_latitude.hashCode() : 0);
      result = result * 37 + (end_longitude != null ? end_longitude.hashCode() : 0);
      result = result * 37 + (trip_distance != null ? trip_distance.hashCode() : 0);
      result = result * 37 + (trip_time != null ? trip_time.hashCode() : 0);
      result = result * 37 + (start_location != null ? start_location.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<TripData> {

    public Double start_latitude;
    public Double start_longitude;
    public List<Double> on_way_latitude;
    public List<Double> on_way_longitude;
    public Double end_latitude;
    public Double end_longitude;
    public Double trip_distance;
    public Long trip_time;
    public String start_location;

    public Builder() {
    }

    public Builder(TripData message) {
      super(message);
      if (message == null) return;
      this.start_latitude = message.start_latitude;
      this.start_longitude = message.start_longitude;
      this.on_way_latitude = copyOf(message.on_way_latitude);
      this.on_way_longitude = copyOf(message.on_way_longitude);
      this.end_latitude = message.end_latitude;
      this.end_longitude = message.end_longitude;
      this.trip_distance = message.trip_distance;
      this.trip_time = message.trip_time;
      this.start_location = message.start_location;
    }

    public Builder start_latitude(Double start_latitude) {
      this.start_latitude = start_latitude;
      return this;
    }

    public Builder start_longitude(Double start_longitude) {
      this.start_longitude = start_longitude;
      return this;
    }

    public Builder on_way_latitude(List<Double> on_way_latitude) {
      this.on_way_latitude = checkForNulls(on_way_latitude);
      return this;
    }

    public Builder on_way_longitude(List<Double> on_way_longitude) {
      this.on_way_longitude = checkForNulls(on_way_longitude);
      return this;
    }

    public Builder end_latitude(Double end_latitude) {
      this.end_latitude = end_latitude;
      return this;
    }

    public Builder end_longitude(Double end_longitude) {
      this.end_longitude = end_longitude;
      return this;
    }

    public Builder trip_distance(Double trip_distance) {
      this.trip_distance = trip_distance;
      return this;
    }

    public Builder trip_time(Long trip_time) {
      this.trip_time = trip_time;
      return this;
    }

    public Builder start_location(String start_location) {
      this.start_location = start_location;
      return this;
    }

    @Override
    public TripData build() {
      return new TripData(this);
    }
  }
}