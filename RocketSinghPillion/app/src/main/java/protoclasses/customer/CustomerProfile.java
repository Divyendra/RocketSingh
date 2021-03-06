// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: E:\workspace_android\workspace_androidstudio\RocketSinghPillion\protos\customer.proto
package customer;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.INT64;
import static com.squareup.wire.Message.Datatype.STRING;

/**
 * Next id - 26
 * Customer profile details
 */
public final class CustomerProfile extends Message {
  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_PHONE_NUMBER = "";
  public static final String DEFAULT_FIRST_NAME = "";
  public static final String DEFAULT_LAST_NAME = "";
  public static final String DEFAULT_DEVICE_TYPE = "";
  public static final String DEFAULT_ALTERNATE_PHONE = "";
  public static final String DEFAULT_EMAIL_ID = "";
  public static final String DEFAULT_ALTERNATE_EMAIL_ID = "";
  public static final String DEFAULT_STREET_ADDRESS = "";
  public static final String DEFAULT_LANDMARK = "";
  public static final String DEFAULT_SUB_LOCALITY = "";
  public static final String DEFAULT_LOCALITY = "";
  public static final String DEFAULT_CITY = "";
  public static final String DEFAULT_STATE = "";
  public static final String DEFAULT_COUNTRY = "";
  public static final String DEFAULT_ZIP_CODE = "";
  public static final String DEFAULT_LATITUDE = "";
  public static final String DEFAULT_LONGITUDE = "";
  public static final String DEFAULT_FB_PAGE = "";
  public static final Long DEFAULT_DATE_CREATED = 0L;
  public static final String DEFAULT_PASSWORD = "";
  public static final String DEFAULT_DEVICE_TOKEN = "";
  public static final String DEFAULT_REFERRAL_CODE = "";

  @ProtoField(tag = 1, type = STRING)
  public final String phone_number;

  @ProtoField(tag = 2, type = STRING)
  public final String first_name;

  @ProtoField(tag = 3, type = STRING)
  public final String last_name;

  @ProtoField(tag = 4, type = STRING)
  public final String device_type;

  @ProtoField(tag = 6, type = STRING)
  public final String alternate_phone;

  @ProtoField(tag = 7, type = STRING)
  public final String email_id;

  @ProtoField(tag = 8, type = STRING)
  public final String alternate_email_id;

  /**
   * Address details
   */
  @ProtoField(tag = 9, type = STRING)
  public final String street_address;

  @ProtoField(tag = 10, type = STRING)
  public final String landmark;

  @ProtoField(tag = 11, type = STRING)
  public final String sub_locality;

  @ProtoField(tag = 12, type = STRING)
  public final String locality;

  @ProtoField(tag = 13, type = STRING)
  public final String city;

  @ProtoField(tag = 14, type = STRING)
  public final String state;

  @ProtoField(tag = 15, type = STRING)
  public final String country;

  @ProtoField(tag = 16, type = STRING)
  public final String zip_code;

  @ProtoField(tag = 17, type = STRING)
  public final String latitude;

  @ProtoField(tag = 18, type = STRING)
  public final String longitude;

  /**
   * Online profile
   */
  @ProtoField(tag = 20, type = STRING)
  public final String fb_page;

  /**
   * Date on which this Record was created
   */
  @ProtoField(tag = 22, type = INT64)
  public final Long date_created;

  /**
   * Account password & device token given by GCM
   */
  @ProtoField(tag = 23, type = STRING)
  public final String password;

  @ProtoField(tag = 24, type = STRING)
  public final String device_token;

  @ProtoField(tag = 25, type = STRING)
  public final String referral_code;

  public CustomerProfile(String phone_number, String first_name, String last_name, String device_type, String alternate_phone, String email_id, String alternate_email_id, String street_address, String landmark, String sub_locality, String locality, String city, String state, String country, String zip_code, String latitude, String longitude, String fb_page, Long date_created, String password, String device_token, String referral_code) {
    this.phone_number = phone_number;
    this.first_name = first_name;
    this.last_name = last_name;
    this.device_type = device_type;
    this.alternate_phone = alternate_phone;
    this.email_id = email_id;
    this.alternate_email_id = alternate_email_id;
    this.street_address = street_address;
    this.landmark = landmark;
    this.sub_locality = sub_locality;
    this.locality = locality;
    this.city = city;
    this.state = state;
    this.country = country;
    this.zip_code = zip_code;
    this.latitude = latitude;
    this.longitude = longitude;
    this.fb_page = fb_page;
    this.date_created = date_created;
    this.password = password;
    this.device_token = device_token;
    this.referral_code = referral_code;
  }

  private CustomerProfile(Builder builder) {
    this(builder.phone_number, builder.first_name, builder.last_name, builder.device_type, builder.alternate_phone, builder.email_id, builder.alternate_email_id, builder.street_address, builder.landmark, builder.sub_locality, builder.locality, builder.city, builder.state, builder.country, builder.zip_code, builder.latitude, builder.longitude, builder.fb_page, builder.date_created, builder.password, builder.device_token, builder.referral_code);
    setBuilder(builder);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof CustomerProfile)) return false;
    CustomerProfile o = (CustomerProfile) other;
    return equals(phone_number, o.phone_number)
        && equals(first_name, o.first_name)
        && equals(last_name, o.last_name)
        && equals(device_type, o.device_type)
        && equals(alternate_phone, o.alternate_phone)
        && equals(email_id, o.email_id)
        && equals(alternate_email_id, o.alternate_email_id)
        && equals(street_address, o.street_address)
        && equals(landmark, o.landmark)
        && equals(sub_locality, o.sub_locality)
        && equals(locality, o.locality)
        && equals(city, o.city)
        && equals(state, o.state)
        && equals(country, o.country)
        && equals(zip_code, o.zip_code)
        && equals(latitude, o.latitude)
        && equals(longitude, o.longitude)
        && equals(fb_page, o.fb_page)
        && equals(date_created, o.date_created)
        && equals(password, o.password)
        && equals(device_token, o.device_token)
        && equals(referral_code, o.referral_code);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = phone_number != null ? phone_number.hashCode() : 0;
      result = result * 37 + (first_name != null ? first_name.hashCode() : 0);
      result = result * 37 + (last_name != null ? last_name.hashCode() : 0);
      result = result * 37 + (device_type != null ? device_type.hashCode() : 0);
      result = result * 37 + (alternate_phone != null ? alternate_phone.hashCode() : 0);
      result = result * 37 + (email_id != null ? email_id.hashCode() : 0);
      result = result * 37 + (alternate_email_id != null ? alternate_email_id.hashCode() : 0);
      result = result * 37 + (street_address != null ? street_address.hashCode() : 0);
      result = result * 37 + (landmark != null ? landmark.hashCode() : 0);
      result = result * 37 + (sub_locality != null ? sub_locality.hashCode() : 0);
      result = result * 37 + (locality != null ? locality.hashCode() : 0);
      result = result * 37 + (city != null ? city.hashCode() : 0);
      result = result * 37 + (state != null ? state.hashCode() : 0);
      result = result * 37 + (country != null ? country.hashCode() : 0);
      result = result * 37 + (zip_code != null ? zip_code.hashCode() : 0);
      result = result * 37 + (latitude != null ? latitude.hashCode() : 0);
      result = result * 37 + (longitude != null ? longitude.hashCode() : 0);
      result = result * 37 + (fb_page != null ? fb_page.hashCode() : 0);
      result = result * 37 + (date_created != null ? date_created.hashCode() : 0);
      result = result * 37 + (password != null ? password.hashCode() : 0);
      result = result * 37 + (device_token != null ? device_token.hashCode() : 0);
      result = result * 37 + (referral_code != null ? referral_code.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<CustomerProfile> {

    public String phone_number;
    public String first_name;
    public String last_name;
    public String device_type;
    public String alternate_phone;
    public String email_id;
    public String alternate_email_id;
    public String street_address;
    public String landmark;
    public String sub_locality;
    public String locality;
    public String city;
    public String state;
    public String country;
    public String zip_code;
    public String latitude;
    public String longitude;
    public String fb_page;
    public Long date_created;
    public String password;
    public String device_token;
    public String referral_code;

    public Builder() {
    }

    public Builder(CustomerProfile message) {
      super(message);
      if (message == null) return;
      this.phone_number = message.phone_number;
      this.first_name = message.first_name;
      this.last_name = message.last_name;
      this.device_type = message.device_type;
      this.alternate_phone = message.alternate_phone;
      this.email_id = message.email_id;
      this.alternate_email_id = message.alternate_email_id;
      this.street_address = message.street_address;
      this.landmark = message.landmark;
      this.sub_locality = message.sub_locality;
      this.locality = message.locality;
      this.city = message.city;
      this.state = message.state;
      this.country = message.country;
      this.zip_code = message.zip_code;
      this.latitude = message.latitude;
      this.longitude = message.longitude;
      this.fb_page = message.fb_page;
      this.date_created = message.date_created;
      this.password = message.password;
      this.device_token = message.device_token;
      this.referral_code = message.referral_code;
    }

    public Builder phone_number(String phone_number) {
      this.phone_number = phone_number;
      return this;
    }

    public Builder first_name(String first_name) {
      this.first_name = first_name;
      return this;
    }

    public Builder last_name(String last_name) {
      this.last_name = last_name;
      return this;
    }

    public Builder device_type(String device_type) {
      this.device_type = device_type;
      return this;
    }

    public Builder alternate_phone(String alternate_phone) {
      this.alternate_phone = alternate_phone;
      return this;
    }

    public Builder email_id(String email_id) {
      this.email_id = email_id;
      return this;
    }

    public Builder alternate_email_id(String alternate_email_id) {
      this.alternate_email_id = alternate_email_id;
      return this;
    }

    /**
     * Address details
     */
    public Builder street_address(String street_address) {
      this.street_address = street_address;
      return this;
    }

    public Builder landmark(String landmark) {
      this.landmark = landmark;
      return this;
    }

    public Builder sub_locality(String sub_locality) {
      this.sub_locality = sub_locality;
      return this;
    }

    public Builder locality(String locality) {
      this.locality = locality;
      return this;
    }

    public Builder city(String city) {
      this.city = city;
      return this;
    }

    public Builder state(String state) {
      this.state = state;
      return this;
    }

    public Builder country(String country) {
      this.country = country;
      return this;
    }

    public Builder zip_code(String zip_code) {
      this.zip_code = zip_code;
      return this;
    }

    public Builder latitude(String latitude) {
      this.latitude = latitude;
      return this;
    }

    public Builder longitude(String longitude) {
      this.longitude = longitude;
      return this;
    }

    /**
     * Online profile
     */
    public Builder fb_page(String fb_page) {
      this.fb_page = fb_page;
      return this;
    }

    /**
     * Date on which this Record was created
     */
    public Builder date_created(Long date_created) {
      this.date_created = date_created;
      return this;
    }

    /**
     * Account password & device token given by GCM
     */
    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder device_token(String device_token) {
      this.device_token = device_token;
      return this;
    }

    public Builder referral_code(String referral_code) {
      this.referral_code = referral_code;
      return this;
    }

    @Override
    public CustomerProfile build() {
      return new CustomerProfile(this);
    }
  }
}
