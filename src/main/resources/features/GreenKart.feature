Feature: GreenKart

  @GreenKart1
  Scenario: User launch the GreenKart Application
    Given user launch the GreenKart Application
    When  user clicks on Add to cart button
    And  user clicks on Cart
    Then  user clicks on proceed checkout
    Then  user verify is there any code in enter promo code text box
#    And clicks on apply button
#    And  user verify choose country there or not
#    And user clicks on Drop down and select country
#    And user clicks on check box
#    And user clicks on proceed button
#    And User verify the text
