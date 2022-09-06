package tests;

import appmanager.HelperBase;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import pages.GreenKartPage;

public class Greenkart_StepDef extends TestBase  {
    HelperBase helperBase = new HelperBase();


    @Autowired
    GreenKartPage greenKartPage;



    @Given("^user launch the GreenKart Application$")
    public void user_launch_the_GreenKart_Application() throws Throwable {
        helperBase.launchApplication();

    }

    @When("^user clicks on Add to cart button$")
    public void user_clicks_on_Add_to_cart_button() throws Throwable {

    }

    @When("^user clicks on Cart$")
    public void user_clicks_on_Cart() throws Throwable {
        greenKartPage.select_items();

    }

    @Then("^user clicks on proceed checkout$")
    public void user_clicks_on_proceed_checkout() throws Throwable {

    }

    @Then("^user verify is there any code in enter promo code text box$")
    public void user_verify_is_there_any_code_in_enter_promo_code_text_box() throws Throwable {

    }

    @Then("^clicks on apply button$")
    public void clicks_on_apply_button() throws Throwable {

    }

    @Then("^user verify choose country there or not$")
    public void user_verify_choose_country_there_or_not() throws Throwable {

    }

    @Then("^user clicks on Drop down and select country$")
    public void user_clicks_on_Drop_down_and_select_country() throws Throwable {

    }

    @Then("^user clicks on check box$")
    public void user_clicks_on_check_box() throws Throwable {

    }

    @Then("^user clicks on proceed button$")
    public void user_clicks_on_proceed_button() throws Throwable {

    }

    @Then("^User verify the text$")
    public void user_verify_the_text() throws Throwable {

    }




}
