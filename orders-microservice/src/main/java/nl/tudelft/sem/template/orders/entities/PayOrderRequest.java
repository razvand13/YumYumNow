package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.Objects;

/**
 * PayOrderRequest
 */

@JsonTypeName("payOrder_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-12-13T10:24:09.051695300+01:00[Europe/Amsterdam]")
public class PayOrderRequest {

  private String paymentInformation;

  private PaymentEntity paymentOption = PaymentEntity.IDEAL;

  /**
   * Get paymentInformation
   * @return paymentInformation
  */
  
  @Schema(name = "paymentInformation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("paymentInformation")
  public String getPaymentInformation() {
    return paymentInformation;
  }

  public void setPaymentInformation(String paymentInformation) {
    this.paymentInformation = paymentInformation;
  }

  /**
   * Get paymentOption
   * @return paymentOption
  */
  @Valid 
  @Schema(name = "paymentOption", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("paymentOption")
  public PaymentEntity getPaymentOption() {
    return paymentOption;
  }

  public void setPaymentOption(PaymentEntity paymentOption) {
    this.paymentOption = paymentOption;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PayOrderRequest payOrderRequest = (PayOrderRequest) o;
    return Objects.equals(this.paymentInformation, payOrderRequest.paymentInformation) &&
        Objects.equals(this.paymentOption, payOrderRequest.paymentOption);
  }

  @Override
  public int hashCode() {
    return Objects.hash(paymentInformation, paymentOption);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PayOrderRequest {\n");
    sb.append("    paymentInformation: ").append(toIndentedString(paymentInformation)).append("\n");
    sb.append("    paymentOption: ").append(toIndentedString(paymentOption)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

