// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: E:\workspace_android\workspace_androidstudio\RocketSinghPillion\protos\MobileResponse.proto
package mobileresponse;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import customer.CustomerProfile;

import static com.squareup.wire.Message.Datatype.STRING;

public final class LoginResponseCustomer extends Message {
  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_STATUS = "";

  @ProtoField(tag = 1, type = STRING)
  public final String status;

  @ProtoField(tag = 2)
  public final CustomerProfile cust_profile;

  public LoginResponseCustomer(String status, CustomerProfile cust_profile) {
    this.status = status;
    this.cust_profile = cust_profile;
  }

  private LoginResponseCustomer(Builder builder) {
    this(builder.status, builder.cust_profile);
    setBuilder(builder);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof LoginResponseCustomer)) return false;
    LoginResponseCustomer o = (LoginResponseCustomer) other;
    return equals(status, o.status)
        && equals(cust_profile, o.cust_profile);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = status != null ? status.hashCode() : 0;
      result = result * 37 + (cust_profile != null ? cust_profile.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<LoginResponseCustomer> {

    public String status;
    public CustomerProfile cust_profile;

    public Builder() {
    }

    public Builder(LoginResponseCustomer message) {
      super(message);
      if (message == null) return;
      this.status = message.status;
      this.cust_profile = message.cust_profile;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder cust_profile(CustomerProfile cust_profile) {
      this.cust_profile = cust_profile;
      return this;
    }

    @Override
    public LoginResponseCustomer build() {
      return new LoginResponseCustomer(this);
    }
  }
}