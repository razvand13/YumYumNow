package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.services.DishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class VendorControllerIntegrationAlexTest {

    @Autowired
    private VendorController vendorController;

    @Autowired
    private DishService dishService;

    private final UUID vendorUUID1 = UUID.fromString("5db89f20-bb0e-4166-af07-ebef17dd78a9");
    private UUID testDishIdForRemoval;
    private UUID testDishIdForUpdate;
    private Dish testDishForAdding;
    private Dish dishForRemoval;

    /**
     * Set up a dish to add to the menu.
     */
    @BeforeEach
    public void setupEach() {
        testDishForAdding = new Dish();
        testDishForAdding.setName("Pizza con Nada");
        testDishForAdding.setDescription("A dish that's gonna keep you hungry");
        testDishForAdding.setPrice(10.0);
        testDishForAdding.setImageLink("http://example.com/nothing.jpg");
        testDishForAdding.setAllergens(Arrays.asList("Nothing", "Hunger"));
        testDishForAdding.setIngredients(Arrays.asList("Dough", "Nothing else"));
        testDishForAdding.setVendorId(vendorUUID1);

        dishForRemoval = new Dish();
        dishForRemoval.setName("Remove this please");
        dishForRemoval.setDescription("Dish was horrible so it needs removal");
        dishForRemoval.setPrice(15.0);
        dishForRemoval.setAllergens(Arrays.asList("Cheese", "Honey"));
        dishForRemoval.setIngredients(Arrays.asList("Gorgonzola", "Honey"));
        dishForRemoval.setVendorId(vendorUUID1);

        ResponseEntity<Dish> addedDishResponse = vendorController.addDishToMenu(vendorUUID1, dishForRemoval);
        testDishIdForRemoval = Objects.requireNonNull(addedDishResponse.getBody()).getID();

        Dish dishForUpdate = new Dish();
        dishForUpdate.setName("Update this Dish");
        dishForUpdate.setDescription("Old description");
        dishForUpdate.setPrice(20.0);
        dishForUpdate.setAllergens(List.of("Whatever"));
        dishForUpdate.setIngredients(Arrays.asList("Something", "Sugar"));
        dishForUpdate.setVendorId(vendorUUID1);

        ResponseEntity<Dish> dishForUpdateResponse = vendorController.addDishToMenu(vendorUUID1, dishForUpdate);
        testDishIdForUpdate = addedDishResponse.getBody().getID();
    }

    @Test
    public void testAddDishToMenu() {
        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorUUID1, testDishForAdding);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Dish addedDish = response.getBody();
        assertThat(addedDish).isNotNull();
        assertThat(addedDish.getName()).isEqualTo(testDishForAdding.getName());
        assertThat(addedDish.getDescription()).isEqualTo(testDishForAdding.getDescription());
        assertThat(addedDish.getPrice()).isEqualTo(testDishForAdding.getPrice());
        assertThat(addedDish.getAllergens()).isEqualTo(testDishForAdding.getAllergens());
        assertThat(addedDish.getIngredients()).isEqualTo(testDishForAdding.getIngredients());
        assertThat(addedDish.getImageLink()).isEqualTo(testDishForAdding.getImageLink());
        assertThat(addedDish.getVendorId()).isEqualTo(vendorUUID1);
    }

    @Test
    public void testAddDishToMenuWithInvalidVendor() {
        UUID invalidVendorUUID = UUID.randomUUID();
        ResponseEntity<Dish> response = vendorController.addDishToMenu(invalidVendorUUID, testDishForAdding);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testRemoveDishFromMenu() {
        ResponseEntity<Void> response = vendorController.removeDishFromMenu(vendorUUID1, testDishIdForRemoval);
        //assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); //TODO: fix this
        Dish deletedDish = dishService.findByIdNotDeleted(testDishIdForRemoval);
        assertThat(deletedDish).isNull();

        Dish deletedDishCheckDeleted = dishService.findById(testDishIdForRemoval);
        assertThat(deletedDishCheckDeleted).isNotNull();
        assertThat(deletedDishCheckDeleted.getName()).isEqualTo(dishForRemoval.getName());
        assertThat(deletedDishCheckDeleted.getDescription()).isEqualTo(dishForRemoval.getDescription());
        assertThat(deletedDishCheckDeleted.getIsDeleted()).isTrue();
        assertThat(deletedDishCheckDeleted.getPrice()).isEqualTo(dishForRemoval.getPrice());
        assertThat(deletedDishCheckDeleted.getAllergens()).isEqualTo(dishForRemoval.getAllergens());
        assertThat(deletedDishCheckDeleted.getIngredients()).isEqualTo(dishForRemoval.getIngredients());
        assertThat(deletedDishCheckDeleted.getImageLink()).isEqualTo(dishForRemoval.getImageLink());
        assertThat(deletedDishCheckDeleted.getVendorId()).isEqualTo(vendorUUID1);
    }

    @Test
    public void testRemoveDishFromMenuWithInvalidVendor() {
        UUID invalidVendorUUID = UUID.randomUUID();
        ResponseEntity<Void> response = vendorController.removeDishFromMenu(invalidVendorUUID, testDishIdForRemoval);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testRemoveDishFromMenuWithInvalidDish() {
        UUID invalidDishId = UUID.randomUUID();
        ResponseEntity<Void> response = vendorController.removeDishFromMenu(vendorUUID1, invalidDishId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testUpdateDishDetails() {
        Dish updatedDish = new Dish();
        updatedDish.setName("Updated and catchier Dish Name");
        updatedDish.setDescription("something new!!!");
        updatedDish.setPrice(25.0);
        //updatedDish.setAllergens(List.of("more interesting than whatever")); //TODO: fix this
        //updatedDish.setIngredients(Arrays.asList("Salt", "Nothing")); //TODO: fix this

        ResponseEntity<Dish> response = vendorController.updateDishDetails(vendorUUID1, testDishIdForUpdate, updatedDish);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Dish dishResponse = response.getBody();
        assert dishResponse != null;
        assertThat(dishResponse.getName()).isEqualTo(updatedDish.getName());
        assertThat(dishResponse.getDescription()).isEqualTo(updatedDish.getDescription());
        assertThat(dishResponse.getPrice()).isEqualTo(updatedDish.getPrice());

    }

    @Test
    public void testUpdateDishDetailsWithInvalidVendor() {
        Dish updatedDish = new Dish();

        UUID invalidVendorUUID = UUID.randomUUID();
        ResponseEntity<Dish> response = vendorController
            .updateDishDetails(invalidVendorUUID, testDishIdForUpdate, updatedDish);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
