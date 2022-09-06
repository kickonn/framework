package pages;

import appmanager.HelperBase;
import org.springframework.beans.factory.annotation.Value;

public class GreenKartPage extends HelperBase {

    @Value("${//div//h4 [text()='Brocolli - 1 Kg']/following-sibling::div/button}")
    public String AddToCart1;

    @Value("${//div/h4[text()='Cauliflower - 1 Kg']/following-sibling::div/button}")
    public  String AddToCart2;

    @Value("${//div/h4[text()='Cucumber - 1 Kg']/following-sibling::div/button}")
    public String AddToCart3;

    @Value("{//img[@src='https://res.cloudinary.com/sivadass/image/upload/v1493548928/icons/bag.png']}")
    public String Cart_Button;




    public  void select_items(){
        sleep(3000);
        clickOn(AddToCart1,"Add To Cart");
        clickOn(AddToCart2,"Add To Cart");
        clickOn(AddToCart3,"Add To Cart");
        clickOn(Cart_Button,"cart");
    }



}
