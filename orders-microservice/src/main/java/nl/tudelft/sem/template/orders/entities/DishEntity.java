package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.*;
import javax.validation.Valid;

/**
 * Dish
 */

@Entity
public class DishEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID ID;
    private String name;

    private String imageLink;

    private Double price;

    @ElementCollection
    private List<String> allergens;

    @ElementCollection
    private List<String> ingredients;

    private String description;

    @ManyToOne
    @JoinColumn(name = "vendorId", referencedColumnName = "ID")
    private Vendor vendor;

    /**
     * Get ID.
     * @return ID
     */
    @Valid
    @Schema(name = "ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("ID")
    public UUID getID() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    /**
     * Get name.
     * @return name
     */

    @Schema(name = "name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get imageLink.
     * @return imageLink
     */

    @Schema(name = "imageLink", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("imageLink")
    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    /**
     * Get price.
     * @return price
     */

    @Schema(name = "price", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("price")
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public DishEntity addAllergensItem(String allergensItem) {
        if (this.allergens == null) {
            this.allergens = new ArrayList<>();
        }
        this.allergens.add(allergensItem);
        return this;
    }

    /**
     * Get allergens.
     * @return allergens
     */

    @Schema(name = "allergens", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("allergens")
    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public DishEntity addIngredientsItem(String ingredientsItem) {
        if (this.ingredients == null) {
            this.ingredients = new ArrayList<>();
        }
        this.ingredients.add(ingredientsItem);
        return this;
    }

    /**
     * Get ingredients.
     * @return ingredients
     */

    @Schema(name = "ingredients", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("ingredients")
    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Get description.
     * @return description
     */

    @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get vendorId.
     * @return vendorId
     */
    @Valid
    @Schema(name = "vendorId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("vendorId")
    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DishEntity dish = (DishEntity) o;
        return Objects.equals(this.ID, dish.ID)
                && Objects.equals(this.name, dish.name)
                && Objects.equals(this.imageLink, dish.imageLink)
                && Objects.equals(this.price, dish.price)
                && Objects.equals(this.allergens, dish.allergens)
                && Objects.equals(this.ingredients, dish.ingredients)
                && Objects.equals(this.description, dish.description)
                && Objects.equals(this.vendor, dish.vendor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, name, imageLink, price, allergens, ingredients, description, vendor);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DishEntity {\n");
        sb.append("    ID: ").append(toIndentedString(ID)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    imageLink: ").append(toIndentedString(imageLink)).append("\n");
        sb.append("    price: ").append(toIndentedString(price)).append("\n");
        sb.append("    allergens: ").append(toIndentedString(allergens)).append("\n");
        sb.append("    ingredients: ").append(toIndentedString(ingredients)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    vendorId: ").append(toIndentedString(vendor)).append("\n");
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

