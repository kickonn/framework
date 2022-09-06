package appmanager;


import io.qameta.allure.Allure;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.KeyEvent;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static appmanager.ApplicationManager.*;
import static appmanager.ExtentCucumberFormatter.*;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;


public class HelperBase {
    public static Connection con;
    public static Statement stmt;
    public static ResultSet resultSet;
    public static WebDriver wd;
    public static WebDriver driver;
    public static Properties obj = new Properties();

    public static boolean screenShotSwitch = false;

    public static String loggedInUser = "";


    public HelperBase(WebDriver wd) {
        HelperBase.wd = wd;
    }


    public HelperBase() {
    }


    public boolean checkLogInUser(String user) {
        ApplicationManager app = null;
//        app = new ApplicationManager(System.getProperty("browser",reader.get("browser.type")));
        if (reader.get("browser.type").toLowerCase().contains("chrome")) {
            app = new ApplicationManager((System.getProperty("browser", BrowserType.CHROME)));
        } else if (reader.get("browser.type").toLowerCase().contains("edge")) {
            app = new ApplicationManager((System.getProperty("browser", BrowserType.EDGE)));
        } else {
            app = new ApplicationManager((System.getProperty("browser", BrowserType.IE)));
        }
        boolean blnLogIn = false;
        WebDriver wd = ApplicationManager.driver;
        if (wd != null) {
            if (isElementPresent(By.xpath("//div[@id='dropdownBasic2']/span[2]"))) {
                WebElement we = wd.findElement(By.xpath("//div[@id='dropdownBasic2']/span[2]"));
                String userLoggged = getText(we);
                if (user.equalsIgnoreCase(userLoggged)) {
                    testStepPassed("User is already logged into the RDS application, Username is -->" + userLoggged);
                    blnLogIn = true;
                }
            }
            if (!blnLogIn) {
                stop();
                try {
                    app.initUrl(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                blnLogIn = true;
                app.initUrl(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return blnLogIn;

    }

    public static boolean checkLogInInvalidUser(String user) {
        ApplicationManager app = new ApplicationManager((System.getProperty("browser", BrowserType.IE)));
        boolean blnLogIn = false;
        try {
            app.initUrl(user);
            blnLogIn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blnLogIn;
    }


    public boolean isElementPresent(WebElement we) {
        try {
            return we.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }


    public boolean isElementPresent(By by) {
        try {
            return getWebDriver().findElement(by).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitTillLoadingCompleted() {
        if (isElementPresent(By.xpath("//*[contains(@class,'spinner')]"))) {
            return elementToBeInvisible(By.xpath("//*[contains(@class,'spinner')]"));
        } else {
            return true;
        }
    }

    public String getText(WebElement we) {
        try {
            if (isElementPresent(we)) {
                return we.getText();
            } else {
                return "";
            }
        } catch (Exception ex) {
            testStepFailed("Unable to get the text element, message is->" + ex.getMessage());
            return "";
        }
    }


    public void validateColumnNamesinTable(WebElement table, List<String> coloumNames) {
        try {
            boolean blnPass = true;
            List<WebElement> weColumnNames = table.findElements(By.xpath(".//thead/tr/th/span[1]"));
            if (weColumnNames.size() == coloumNames.size()) {
                //int i=0;
                for (int i = 0; i < coloumNames.size(); i++) {
                    //for(WebElement we: weColumnNames){
                    weColumnNames = table.findElements(By.xpath(".//thead/tr/th/span[1]"));
                    WebElement we = weColumnNames.get(i);
                    if (getText(we).contains(coloumNames.get(i))) {
                        testStepInfo("Column Name : " + coloumNames.get(i) + " is displayed as expected in Column Number " + (i + 1));
                    } else {
                        blnPass = false;
                        testStepFailed("Column Name : " + coloumNames.get(i) + " is not displayed in Column Number " + (i + 1));
                    }
                }
                if (blnPass) {
                    testStepPassed("All the required columns are displayed in table as expected");
                }
            } else {
                testStepFailed("All the required columns are not displayed in table, Expected Columns is " + coloumNames.size() + ", But actual is ->" + weColumnNames.size());
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while validating the generic elements in page, Message is->" + ex.getMessage());
        }
    }

    public static void waitFor(String object) {
        try {
            WebDriverWait wait = new WebDriverWait(wd, 360);
            By locator = xpath(obj.getProperty(object));
            wait.until(elementToBeClickable(locator));
        } catch (Exception ex) {
            //do nothing
        }
    }

    public static void waitForAlertDismiss() {
        for (int i = 0; i < 10; i++) {
            try {
                WebDriver wd = getWebDriver();
                wd.switchTo().defaultContent();
                break;
            } catch (UnhandledAlertException ex) {
                sleep(500);
            }
        }
    }


    public static void waitFor(By by) {
        try {
            WebDriverWait wait = new WebDriverWait(wd, 60);
            wait.until(presenceOfElementLocated(by));
        } catch (Exception ex) {
            //do nothing
        }
    }

    public void clickOnTillDisapears(String obj, String elemName) {
        try {
            sleep(1000);
            while (isElementPresent(obj)) {
                jsClick(obj, "Update Button");
                sleep(2000);
            }
            testStepInfo("Clicked on Element-" + elemName);
        } catch (Exception ex) {
            testStepFailed("Unable to Click on Element- " + elemName);
        }
    }

    public void clickOn(String obj, String elemName) {
        try {
            sleep(1000);
            WebElement we = getWebElement(obj);
            we.click();
            testStepInfo("Clicked on Element-" + elemName);
        } catch (Exception ex) {
            testStepFailed("Unable to Click on Element- " + elemName);
        }
    }

    public static void type(String object, String data) {
        By locator = xpath(obj.getProperty(object));
        if (data != null) {
            String existingText = wd.findElement(locator).getAttribute("value");
            if (!data.equals(existingText)) {
                wd.findElement(locator).click();
                wd.findElement(locator).clear();
                wd.findElement(locator).click();
                wd.findElement(locator).sendKeys(data);
            }
        }
    }


    public static void type(By by, String data) {

        try {
            WebElement elem = wd.findElement(by);
            String existingText = elem.getAttribute("value");
            if (!data.equals(existingText)) {
                elem.clear();
                elem.sendKeys(data);
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }

    public static void typeValue(String object, String data) {
        By locator = xpath(obj.getProperty(object));
        if (data != null) {
            String existingText = wd.findElement(locator).getAttribute("value");
            if (!data.equals(existingText)) {
                wd.findElement(locator).click();
                wd.findElement(locator).clear();
                wd.findElement(locator).click();
                wd.findElement(locator).sendKeys(Keys.BACK_SPACE);
                wd.findElement(locator).sendKeys(Keys.DELETE);
                wd.findElement(locator).sendKeys(data);
                wd.findElement(locator).sendKeys(Keys.TAB);
            }
        }
    }

    public static void typeText(String object, String data) {
        try {
            By locator = xpath(obj.getProperty(object));
            if (data != null) {
                String existingText = wd.findElement(locator).getAttribute("value");
                if (!data.equals(existingText)) {
                    wd.findElement(locator).click();
                    sleep(2000);
                    wd.findElement(locator).sendKeys(Keys.END);
                    wd.findElement(locator).sendKeys(Keys.CONTROL + "a");
                    wd.findElement(locator).sendKeys(Keys.DELETE);
                    wd.findElement(locator).sendKeys(data);
                }
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }

    public static void checkSpecialChracter(String object, String data) {
        try {
            By locator = xpath(obj.getProperty(object));

            wd.findElement(locator).click();
            sleep(2000);
            wd.findElement(locator).sendKeys(Keys.END);
            wd.findElement(locator).sendKeys(Keys.CONTROL + "a");
            wd.findElement(locator).sendKeys(Keys.DELETE);
            wd.findElement(locator).sendKeys(data);

            String existingText = wd.findElement(locator).getAttribute("value");
            if (existingText.equals("")) {
                testStepPassed("Special Chracter and alphabet is not allowed as expected " + existingText);

            } else {
                testStepFailed("Special Chracter and alphabet is  allowed  " + existingText);

            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }

    public String validateErrorMessageForField(WebElement elem, String errorMessage) {
        try {
            WebElement we = elem.findElement(By.xpath("./following-sibling::div/div[1]"));
            return we.getText();
        } catch (Exception ex) {
            return "";
        }
    }

    public void selectFromDropdown(String object, String strVisibleText, String strElementName) {
        try {
            WebElement element = getWebElement(object);
            Select sel = new Select(element);
            sel.selectByVisibleText(strVisibleText);
            testStepInfo("Selected the value->" + strVisibleText + " from the dropdown " + strElementName);

        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    public static String text(String object) {
        String text = "";
        try {
            text = wd.findElement(xpath(obj.getProperty(object))).getText();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return text;
    }


    public String gettext(String object) {
        String text = "";
        try {
            sleep(1000);
            text = getWebElement(object).getAttribute("value");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return text;
    }


    public String getTextValue(String object) {
        String text = "";
        try {
            text = getWebElement(object).getText();
        } catch (Exception e) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), e);
        }
        return text;
    }

    public static boolean waitForElementNotPresent(String object) {
        try {
            wd.findElement(xpath(obj.getProperty(object)));
            return false;
        } catch (NoSuchElementException e) {
            return true;
        }
    }


    public boolean isElementPresent(String obj) {
        try {
            return getWebElement(obj).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEnabled(String object) {
        try {
            WebElement we = getWebElement(object);

            we.isEnabled();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static void selectFromDropdownInTs(String object, String strElementName, String strVisibleText) {
        try {
            WebElement element = wd.findElement(xpath(obj.getProperty(object)));
            Select sel = new Select(element);
            sel.selectByVisibleText(strVisibleText);
            testStepInfo("Selected the value->" + strVisibleText + " from the dropdown " + strElementName);

        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }


    public void jsEnterText(String object, String text, String elemName) {
        try {
            WebElement we = getWebElement(object);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].value='" + text + "';", we);
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
    }

    public void jsEnterText(WebElement object, String text, String elemName) {
        try {
            sleep(2000);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].value='" + text + "';", object);
            sleep(2000);
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
    }

    public static String selectcurrentDate() {
        String CurrentDate = "";
        try {
            SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
            Date date = new Date();
            CurrentDate = currentDate.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CurrentDate;

    }

    public void clear(String object) {
        try {
            WebElement we = getWebElement(object);
            we.sendKeys(Keys.END);
            we.sendKeys(Keys.CONTROL + "a");
            we.sendKeys(Keys.DELETE);
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }

    public static String getMessage() {
        String ErrMsg = wd.findElement(xpath("//ul[@class = 'message']/li[contains(@class,'msg')]")).getText();
        return ErrMsg;
    }

    public void JsClickByVisibleText(String elementName) {
        try {
            WebElement elem = getWebElementByPartialText(elementName);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].click();", elem);
            testStepInfo("Successfully clicked on element -> " + elementName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void JsClick(By by, String elementName) {
        WebElement elem = wd.findElement(by);
        JavascriptExecutor js = (JavascriptExecutor) wd;
        js.executeScript("arguments[0].click();", elem);
        testStepInfo("Successfully clicked on element->" + elementName);
    }

    public String getRandomAlphabetString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public String getRandomAlphaNumbericString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public String getRandomNumbericString(int length) {
        return RandomStringUtils.randomNumeric(length);
    }


    public static void sleep(int miliSec) {
        try {
            Thread.sleep(miliSec);
        } catch (InterruptedException e) {
            // do nothing
        }
    }


    public void validateFieldsErrorMessage(String object, String data, String errormessage) {
        try {
            WebElement locator = getWebElement(object);

            locator.click();
            sleep(2000);
            locator.sendKeys(Keys.END);
            locator.sendKeys(Keys.CONTROL + "a");
            locator.sendKeys(Keys.DELETE);
            locator.sendKeys(Keys.HOME);
            locator.sendKeys(data);
            String appMesssage = getWebElement(By.xpath("//div[contains(text(),'" + errormessage + "')]")).getText();
            if (appMesssage.trim().contains(errormessage)) {
                testStepPassed("Application error message is displayed as expected-> " + errormessage);
            } else {
                testStepFailed("Application Error Message-> " + appMesssage + " is not as expected Message -> " + errormessage);
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }



    public boolean validateSearchResults() {
        if (isElementPresent("table")) {
            List<WebElement> row = wd.findElements(xpath("//div/table[contains(@class,'table')]//tbody/tr"));
            return row.size() > 0;
        } else
            return false;
    }

    public void validateRecordsforShowDropDown(String value) {
        int Value = Integer.parseInt(value);
        if (isElementPresent("table")) {
            List<WebElement> row = wd.findElements(xpath("//div/table[contains(@class,'table')]//tbody/tr"));
            int rowSize = row.size();
            if (rowSize == Value) {
                testStepPassed("Sucessfully validated for " + value + " records to be displayed in the page");
                takeScreenshot();
            }
        } else
            testStepFailed("No records found");
    }

    public static boolean fileExists(String fileName) {
        for (int i = 0; i < 30; i++) {
            File file = new File(fileName);
            if (file.exists() && file.isFile()) {
                return true;
            } else {
                sleep(3000);
            }
        }
        return false;
    }

    public void clickOnParticularRowofTable(String value) {
        boolean record = validateSearchResults();
        if (record) {
            if (isElementPresent("table")) {
                WebElement Mnemonic = wd.findElement(xpath("//table/tbody/tr/td[text()='" + value + "']/parent::tr/td[2]"));
                String custNo = Mnemonic.getText();
                JsClick(xpath("//table/tbody/tr/td[text()='" + custNo + "']"), "record");
                testStepPassed("Successfully clicked on the record");
                takeScreenshot();
                sleep(2000);
            } else {
                testStepFailed("Failed to click on the record");
            }
        } else {
            testStepFailed("No records are found with the search filter data");
        }
    }

    public void selectRandomRowFromTable() {
        try {
            List<WebElement> lst = getWebElements(By.xpath("//table[contains(@class,table)]//tbody/tr"));
            int table_size = lst.size();
            if (table_size >= 1) {
                Random number = new Random();
                //     int row_Number = number.nextInt(table_size) + 1;
                WebElement we = getWebElement(By.xpath("//table[contains(@class,table)]//tbody/tr[4]"));
                JsClick(we, "element");

            } else {
                //Check for no data message
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }


    public void validateErrormessage(String object, String expMessage) {
        try {
            waitFor(object);
            String message = getWebElement(object).getText();
            if (message.toLowerCase().contains(expMessage.toLowerCase())) {
                testStepPassed("Expected message is Displayed ->" + message);
            } else {
                testStepFailed("Expected Message->" + expMessage + " is not displayed,instead " + message + " is displayed");

            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }

    }



    public void validateErrormessage(String expMessage) {
        try {

            WebElement ele = getWebElementByPartialText(expMessage);
            String message = ele.getText();
            if (message.toLowerCase().contains(expMessage.toLowerCase())) {
                testStepPassed("Expected message is Displayed ->" + message);
            } else {
                testStepFailed("Expected Message->" + expMessage + " is not displayed,instead " + message + " is displayed");

            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }

    }

    public boolean verifyAmountPledgeRecords() {
        List<WebElement> recordsCount = wd.findElements(xpath("//table[@id='IncreasePledgeTable']//tr"));
        int size = recordsCount.size();
        return size > 0;
    }

    // returns WebElement object please use instead of wd.findElement(By.locator) method  (created by Akbar 02.01.19)
    public WebElement getWebElement(String object) {
        return getWebElement(By.xpath(object));
    }


    // returns WebElement object please use instead of wd.findElement(By.locator) method  (created by Akbar 02.01.19)
    public List<WebElement> getWebElements(String object) {
        return getWebElements(By.xpath(object));
    }

    // returns WebElement object please use this method when you have multiple similar web objects with different values
    // Use when you going to get the web object by xpath containing text (created by Akbar 02.01.19)
    public WebElement getWebElementByPartialText(String objectValue) {
        return getWebElement(By.xpath("//*[contains(text(), '" + objectValue + "')]"));
    }


    public WebElement getWebElementByPartialText(String objectValue, String tagName) {
        return getWebElement(By.xpath("//" + tagName + "[contains(text(), '" + objectValue + "')]"));
    }

    // returns WebElement object please use this method when you have multiple similar web objects with different values
    // Use when you going to get the web object by xpath containing text (created by Akbar 02.01.19)
    public WebElement getWebElementByText(String objectValue) {
        return getWebElement(By.xpath("//*[text()='" + objectValue + "']"));
    }

    public WebElement getWebElementByText(String objectValue, String tagName) {
        return getWebElement(By.xpath("//" + tagName + "[text()='" + objectValue + "']"));
    }

    public WebElement getWebElement(By locator) {

        FluentWait<WebDriver> wait = new FluentWait<>(getWebDriver())
                .withTimeout(java.time.Duration.ofSeconds(20))
                .pollingEvery(java.time.Duration.ofSeconds(5))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .ignoring(ElementNotVisibleException.class)
                .ignoring(ElementNotSelectableException.class)
                .ignoring(ElementNotInteractableException.class)
                .ignoring(ElementNotVisibleException.class)
                .ignoring(ElementClickInterceptedException.class)
                .ignoring(NullPointerException.class);
        WebElement element = wait.until(new com.google.common.base.Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                return driver.findElement(locator);
            }
        });

        //highlightAndTakeScreenShot(element);
        return element;
    }

    // returns list of WebElement objects please use instead of wd.findElements(By.locator) method (created by Akbar)
    public List<WebElement> getWebElements(By locator) {

        FluentWait<WebDriver> wait = new FluentWait<WebDriver>(getWebDriver())
                .withTimeout(java.time.Duration.ofSeconds(15))
                .pollingEvery(java.time.Duration.ofSeconds(3))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .ignoring(ElementNotVisibleException.class)
                .ignoring(ElementNotSelectableException.class)
                .ignoring(ElementNotInteractableException.class)
                .ignoring(ElementNotVisibleException.class);

        List<WebElement> element = wait.until(new com.google.common.base.Function<WebDriver, List<WebElement>>() {
            public List<WebElement> apply(WebDriver driver) {
                return driver.findElements(locator);
            }
        });
        return element;
    }




    public String getClass(WebElement we) {
        try {
            return we.getAttribute("class");
        } catch (Exception ex) {
            return "";
        }
    }

    public void jsclear(String elem, String elementName) {
        try {
            WebElement weElem = getWebElement(elem);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].clear();", weElem);
            testStepInfo("Successfully cleared on element->" + elementName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void jsClickByVisibleText(String elementName) {
        try {
            WebElement elem = getWebElementByPartialText(elementName);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].click();", elem);
            testStepInfo("Successfully clicked on element -> " + elementName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void selectRecordFromTable(String objTable, int rowNum) {
        WebElement tbl = getWebElement(objTable);
        List<WebElement> weRows = tbl.findElements(By.xpath("//tbody//tr//a"));
        int rowCount = weRows.size();
        if (rowCount >= rowNum) {
            clickOn(weRows.get(rowNum - 1), rowNum + " row in the table");
        } else {
            testStepFailed("Total row count is only " + rowCount + " , hence row " + rowNum + " is not available");
        }
        waitTillLoadingCompleted();
    }

    public void clickOn(WebElement we, String elemName) {
        try {
            we.click();
            testStepInfo("Clicked on Element-" + elemName);
        } catch (Exception ex) {
            testStepFailed("Unable to Click on Element- " + elemName + ", Exception is ->" + ex.getMessage());
        }
    }

    public void validateDefaultSorting(int columnNumber, String ColumName, String Type, String TableName, String sortOrder) {
        try {
            WebDriver wd = getWebDriver();
            List<WebElement> row = wd.findElements(By.xpath("//div[contains(@class,'table')]/table//tr"));
            sleep(4000);
            int i = row.size();
            if (i > 0) {
                sleep(3000);
                if (sortOrder.toLowerCase().contains("a")) {
                    validateAscendingOrder(columnNumber, ColumName, Type);
                } else {
                    validateDescendingOrder(columnNumber, ColumName, Type);
                }
            } else {
                testStepInfo("No records found,Hence Sorting functionality cannot be validated");

            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while validating the default sorting functionality, Message is ->" + ex.getMessage());
        }
    }


    public void ValidateSortingFunctionality(int columnNumber, String ColumName, String Type, String AppName) {
        try {
            WebDriver wd = getWebDriver();
            List<WebElement> row = wd.findElements(By.xpath("//div[contains(@class,'table')]/table//tr"));
            int i = row.size();
            if (i > 0) {
                sleep(3000);
                By bySort = By.xpath("//div[contains(@class,'table')]/table//span[contains(text(),'" + ColumName + "')]/following::span[contains(@class,'sort')][1]");
                if (!getClass(getWebElement(bySort)).contains("asc")) {
                    JsClick(getWebElement(bySort), "Ascending");
                }
                sleep(5000);
                validateAscendingOrder(columnNumber, ColumName, Type);
                if (!getClass(getWebElement(bySort)).contains("desc")) {
                    JsClick(getWebElement(bySort), "Descending");
                }
                sleep(5000);
                validateDescendingOrder(columnNumber, ColumName, Type);
                sleep(3000);
            } else {
                testStepInfo("No records found,Hence Sorting functionality cannot be validated");

            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while validating the sorting functionality, Message is ->" + ex.getMessage());
        }
    }

    public int searchValueInTable(String firstColumnValue, String secondColumnValue) throws InterruptedException {
        boolean status = false;
        int rowNumber = 0;
        List<WebElement> row = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tbody/tr"));
        int rowCount = row.size();
        if (rowCount > 0) {
            Thread.sleep(3000);
            for (int i = 1; i <= rowCount; i++) {
                String appFirstColumnValue = wd.findElement(xpath("//table[contains(@class,'table table-bordered')]//tbody/tr[" + i + "]/td[1]/a")).getText();
                String newelementName2 = wd.findElement(xpath("//table[contains(@class,'table table-bordered')]//tbody/tr[" + i + "]/td[2]")).getText();
                if (appFirstColumnValue.contains(firstColumnValue) && newelementName2.contains(secondColumnValue)) {
                    status = true;
                    testStepPassed("Values Found in Row number " + i);
                    rowNumber = i;
                    break;
                } else {
                    status = false;
                }
            }
            if (!status) {
                testStepFailed("Values  Not Found in Table");
            }
        } else {
            testStepInfo("No records found,Hence Can not Search For Values");
        }
        return rowNumber;
    }

    public void validateAscendingOrder(int columnNumber, String ColumName, String Type) {
        try {
            WebDriver wd = getWebDriver();
            int value = columnNumber + 1;
            WebElement icon = wd.findElement(By.xpath("//div[contains(@class,'table')]/table/thead/tr/th[" + value + "]/span[2]"));
            System.out.println("Webelement xpath: " + "//div[contains(@class,'table')]/table/thead/tr/th[" + value + "]/span[2]");
            if (!icon.getAttribute("class").contains("asc")) {
                testStepFailed(ColumName + " column's icon is not changed to Ascending");
            }
            List<WebElement> defaultRow = wd.findElements(By.xpath("//div[contains(@class,'table')]/table//tr"));
//            int itCount = 10;
            int itCount = defaultRow.size() - 1;
            ;
            List<WebElement> elementName = new LinkedList<>();
            if (Type == "STRING") {
                ArrayList<String> obtainedEleList = new ArrayList<>();
                ArrayList<String> resultEleNameList = new ArrayList<>();
                elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                for (int i = 0; i < itCount; i++) {
                    elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                    //String newelementName = (String) ((JavascriptExecutor) wd).executeScript("return arguments[0].text;", elementName.get(i));
                    String newelementName = elementName.get(i).getText().trim();
                    if (!newelementName.trim().equals("")) {
                        obtainedEleList.add(newelementName.toUpperCase().trim());
                        resultEleNameList.add(newelementName.toUpperCase().trim());
                        // obtainedEleList.add(newelementName);
                        //resultEleNameList.add(newelementName);
                    }
                }
                Collections.sort(obtainedEleList);
                System.out.println("Actual: " + resultEleNameList + "\nExpected: " + obtainedEleList);
                if (resultEleNameList.equals(obtainedEleList)) {
                    testStepPassed(ColumName + " column is sorted in Ascending order");
                } else {
                    testStepFailed(ColumName + " column is not sorted in Ascending order");

                }
            }
            if (Type == "STRINGCUST") {
                ArrayList<String> obtainedEleList = new ArrayList<>();
                ArrayList<String> resultEleNameList = new ArrayList<>();
                elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                for (int i = 0; i < itCount; i++) {
                    elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                    //String newelementName = (String) ((JavascriptExecutor) wd).executeScript("return arguments[0].text;", elementName.get(i));
                    String newelementName = elementName.get(i).getText().trim();
                    if (!newelementName.trim().equals("")) {
                        obtainedEleList.add(newelementName);
                        resultEleNameList.add(newelementName);
                    }
                }
                Collections.sort(obtainedEleList);
                if (resultEleNameList.equals(obtainedEleList)) {
                    testStepPassed(ColumName + " column is sorted in Ascending order");
                } else {
                    testStepFailed(ColumName + " column is not sorted in Ascending order");

                }
            } else if (Type == "INTEGER") {
                ArrayList<Integer> obtainedEleList = new ArrayList<>();
                ArrayList<Integer> resultEleNameList = new ArrayList<>();
                elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                int count = elementName.size();
                for (int i = 0; i < itCount; i++) {
                    elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                    String newelementName1 = elementName.get(i).getText().trim();
                    if (newelementName1.contains("-")) {
                        newelementName1 = newelementName1.split("-")[0];
                    }
                    String newelementName = newelementName1.replaceAll(",", "");
                    if (!newelementName.equals("")) {
                        obtainedEleList.add(Integer.parseInt(newelementName));
                        resultEleNameList.add(Integer.parseInt(newelementName));
                    }
                }
                Collections.sort(obtainedEleList);
                if (resultEleNameList.equals(obtainedEleList)) {
                    testStepPassed(ColumName + " column is sorted in Ascending  order");
                } else {
                    testStepFailed(ColumName + " column is not sorted in Ascending order");

                }
            } else if (Type == "DOUBLE") {
                ArrayList<Double> obtainedEleList = new ArrayList<>();
                ArrayList<Double> resultEleNameList = new ArrayList<>();
                elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                int count = elementName.size();
                for (int i = 0; i < itCount; i++) {
                    elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                    String newelementName2 = elementName.get(i).getText().trim();
                    String newelementName1 = newelementName2.replaceAll(",", "");
                    String newelementName = newelementName1.replace("$", "").replace(")", "").replace("(", "-");
                    if (!newelementName.equals("")) {
                        obtainedEleList.add(Double.valueOf(newelementName));
                        resultEleNameList.add(Double.valueOf(newelementName));
                    }
                }
                Collections.sort(obtainedEleList);
                if (resultEleNameList.equals(obtainedEleList)) {
                    testStepPassed(ColumName + " column is sorted in Ascending  order");
                } else {
                    testStepFailed(ColumName + " column is not sorted in Ascending order");

                }
            } else if (Type == "DATE") {
                ArrayList<Date> obtainedEleList = new ArrayList<>();
                ArrayList<Date> resultEleNameList = new ArrayList<>();
                elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                int count = elementName.size();
                for (int i = 0; i < itCount; i++) {
                    elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                    String newelementName = elementName.get(i).getText().trim();
                    SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy");
                    if (!newelementName.equals("")) {
                        try {
                            obtainedEleList.add(date.parse(newelementName));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        try {
                            resultEleNameList.add(date.parse(newelementName));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Collections.sort(obtainedEleList);
                if (resultEleNameList.equals(obtainedEleList)) {
                    testStepPassed(ColumName + " column is sorted in Ascending order");
                } else {
                    testStepFailed(ColumName + " column is not sorted in Ascending order");

                }
            } else if (Type == "DATETIME") {
                ArrayList<Date> obtainedEleList = new ArrayList<>();
                ArrayList<Date> resultEleNameList = new ArrayList<>();
                elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                int count = elementName.size();
                for (int i = 0; i < itCount; i++) {
                    elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                    String newelementName = elementName.get(i).getText().trim();
                    boolean flag = true;
                    SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    if (!newelementName.equals("")) {
                        try {
                            resultEleNameList.add((date.parse(newelementName)));
                            obtainedEleList.add((date.parse(newelementName)));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Collections.sort(resultEleNameList);
                if (resultEleNameList.equals(obtainedEleList)) {
                    testStepPassed(ColumName + " column is sorted in Ascending order");
                } else {
                    testStepFailed(ColumName + " column is not sorted in Ascending  order");

                }
            } else if (Type == "DROPDOWN") {
                ArrayList<String> obtainedEleList = new ArrayList<>();
                ArrayList<String> resultEleNameList = new ArrayList<>();
                elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]/select"));
                for (int i = 0; i < itCount; i++) {
                    elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]/select"));
                    String newelementName = getSelectedOptionFromDropdown(elementName.get(i));
                    if (!newelementName.trim().equals("")) {
                        obtainedEleList.add(newelementName.toUpperCase());
                        resultEleNameList.add(newelementName.toUpperCase());
                    }
                }
                Collections.sort(obtainedEleList);
                if (resultEleNameList.equals(obtainedEleList)) {
                    testStepPassed(ColumName + " column is sorted in Ascending order");
                } else {
                    testStepFailed(ColumName + " column is not sorted in Ascending order");

                }
            }
            if (Type == "CHECKBOX") {
                ArrayList<String> obtainedEleList = new ArrayList<>();
                ArrayList<String> resultEleNameList = new ArrayList<>();
                elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]/input"));
                for (int i = 0; i < itCount; i++) {
                    elementName = wd.findElements(By.xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]/input"));
                    String newelementName;
                    if (elementName.get(i).isSelected()) {
                        newelementName = "B";
                    } else {
                        newelementName = "A";
                    }
                    if (!newelementName.equals("")) {
                        obtainedEleList.add(newelementName.toUpperCase());
                        resultEleNameList.add(newelementName.toUpperCase());
                    }
                }
                Collections.sort(obtainedEleList);

                if (resultEleNameList.equals(obtainedEleList)) {
                    testStepPassed(ColumName + " column is sorted in Descending order");
                } else {
                    testStepFailed(ColumName + " column is not sorted in Descending order");

                }
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while validating the Ascending sorting functionality for column " + ColumName + ", Message is ->" + ex.getMessage());
        }
    }

    public List<WebElement> getSelectedOptionFromdropdown(WebElement dropdown) {
        Select sel = new Select(dropdown);
        return sel.getOptions();
    }

    public String getSelectedOptionFromDropdown(WebElement dropdown) {
        Select sel = new Select(dropdown);
        return sel.getFirstSelectedOption().getText();
    }


    public void validateDescendingOrder(int columnNumber, String ColumName, String Type) {
        WebDriver wd = getWebDriver();
        List<WebElement> defaultRow = wd.findElements(xpath("//div[contains(@class,'table')]/table//tr"));
        int itCount = defaultRow.size() - 1;
        int value = columnNumber + 1;
        List<WebElement> elementName = new LinkedList<>();
        if (Type == "STRING") {
            ArrayList<String> obtainedEleList = new ArrayList<>();
            ArrayList<String> resultEleNameList = new ArrayList<>();
            for (int i = 0; i < itCount; i++) {
                elementName = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                String newelementName = elementName.get(i).getText().trim();
                if (!newelementName.equals("")) {
                    obtainedEleList.add(newelementName.toUpperCase());
                    resultEleNameList.add(newelementName.toUpperCase());
                }
            }
            Collections.sort(obtainedEleList, Collections.reverseOrder());

            if (resultEleNameList.equals(obtainedEleList)) {
                testStepPassed(ColumName + " column is sorted in Descending order");
                takeScreenshot();
            } else {
                testStepFailed(ColumName + " column is not sorted in Descending order");

            }
        } else if (Type == "INTEGER") {
            ArrayList<Integer> obtainedEleList = new ArrayList<>();
            ArrayList<Integer> resultEleNameList = new ArrayList<>();
            for (int i = 0; i < itCount; i++) {
                elementName = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                String newelementName = elementName.get(i).getText().trim();
                if (newelementName.contains("-")) {
                    newelementName = newelementName.split("-")[0];
                }
                if (!newelementName.equals("")) {
                    obtainedEleList.add(Integer.parseInt(newelementName));
                    resultEleNameList.add(Integer.parseInt(newelementName));
                }
            }
            Collections.sort(obtainedEleList, Collections.reverseOrder());
            if (resultEleNameList.equals(obtainedEleList)) {
                testStepPassed(ColumName + " column is sorted in Descending order");
                takeScreenshot();


            } else {
                testStepFailed(ColumName + " column is not sorted in Descending order");

            }
        } else if (Type == "DOUBLE") {
            ArrayList<Double> obtainedEleList = new ArrayList<>();
            ArrayList<Double> resultEleNameList = new ArrayList<>();
            elementName = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
            int count = elementName.size();
            for (int i = 0; i < itCount; i++) {
                elementName = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                String newelementName2 = elementName.get(i).getText().trim();
                String newelementName1 = newelementName2.replaceAll(",", "");
                String newelementName = newelementName1.replace("$", "");
                if (!newelementName.equals("")) {
                    obtainedEleList.add(Double.valueOf(newelementName));
                    resultEleNameList.add(Double.valueOf(newelementName));
                }
            }
            Collections.sort(obtainedEleList, Collections.reverseOrder());
            if (resultEleNameList.equals(obtainedEleList)) {
                testStepPassed(ColumName + " column is sorted in Descending order");
                takeScreenshot();


            } else {
                testStepFailed(ColumName + " column is not sorted in Descending order");
            }
        } else if (Type == "DATE") {
            ArrayList<Date> obtainedEleList = new ArrayList<>();
            ArrayList<Date> resultEleNameList = new ArrayList<>();
            elementName = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
            int count = elementName.size();
            for (int i = 0; i < itCount; i++) {
                elementName = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                String newelementName = elementName.get(i).getText().trim();
                SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy");
                if (!newelementName.equals("")) {
                    try {
                        obtainedEleList.add(date.parse(newelementName));
                        resultEleNameList.add(date.parse(newelementName));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            Collections.sort(obtainedEleList, Collections.reverseOrder());
            if (resultEleNameList.equals(obtainedEleList)) {
                testStepPassed(ColumName + " column is sorted in descending order");
                takeScreenshot();
            } else {
                testStepFailed(ColumName + " column is not sorted in descending  order");

            }
        } else if (Type == "DATETIME") {
            ArrayList<Date> obtainedEleList = new ArrayList<>();
            ArrayList<Date> resultEleNameList = new ArrayList<>();
            elementName = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
            int count = elementName.size();

            for (int i = 0; i < itCount; i++) {
                elementName = wd.findElements(xpath("//table[contains(@class,'table table-bordered')]//tr/td[" + value + "]"));
                String newelementName = elementName.get(i).getText().trim();
                boolean flag = true;
                SimpleDateFormat dateTime = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                if (!newelementName.equals("")) {
                    try {
                        resultEleNameList.add((dateTime.parse(newelementName)));
                        obtainedEleList.add((dateTime.parse(newelementName)));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            Collections.sort(resultEleNameList, Collections.reverseOrder());
            if (resultEleNameList.equals(obtainedEleList)) {

                testStepPassed(ColumName + " column is sorted in Descending   order");
                takeScreenshot();
            } else {
                testStepFailed(ColumName + " column is not sorted in Descending   order");

            }
        }

    }

    public String getPageUrl() {
        try {
            return getWebDriver().getCurrentUrl();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

/*
    public void validateBreadcrumb(String breadcrumb) throws Throwable {
        try {
            boolean blnTrue = true;
            String[] strBreadCrumbs = breadcrumb.split("/");
            List<WebElement> weBreadcrumb = getWebElements(wesBreadCumb);
            int i = 0;
            if (strBreadCrumbs.length == weBreadcrumb.size()) {
                for (WebElement we : weBreadcrumb) {
                    if (we.getText().contains(strBreadCrumbs[i])) {
                        testStepInfo(strBreadCrumbs[i] + " breadcrumb is displayed");
                    } else {
                        testStepFailed(strBreadCrumbs[i] + " breadcrumb is not displayed, instead " + we.getText() + " is displayed ");
                    }
                    i++;
                }
                if (blnTrue) {
                    testStepPassed("All the breadcumbs are displayed as expected ");
                }
                WebElement weBreadcrumbLink1 = getWebElement(By.xpath("//nav[@aria-label='breadcrumb']/ol/li/a[contains(text(),'" + strBreadCrumbs[2] + "')]"));
                JsClick(weBreadcrumbLink1, strBreadCrumbs[2] + " Breadcrumb");
                //verifyPartialLabelDisplayed(strBreadCrumbs[3], "h5");
                if (strBreadCrumbs.length == 4) {
                    getWebDriver().navigate().back();
                    sleep(3000);
                }
                WebElement weBreadcrumbLink2 = getWebElement(By.xpath("//nav[@aria-label='breadcrumb']//a[contains(text(),'" + strBreadCrumbs[0] + "')]"));
                JsClick(weBreadcrumbLink2, strBreadCrumbs[0] + " Breadcrumb");
                sleep(3000);
                if (getPageUrl().endsWith("fhc-hsehld-view")) {
                    testStepPassed("Navigated to home page successfully");
                } else {
                    testStepFailed("Unable to navigate to home page");
                }
            } else {
                testStepFailed("Expected breadcrumb is not displayed in page, Expected is->" + breadcrumb);
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while validating the generic elements in page, Message is->" + ex.getMessage());
        }
    }*/

    public static String getFileName() {
        String timeFile = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss a").format(new Date());
        return timeFile.replace(" ", "");
    }


    public static void verifySelectDeselect(String option, String app) {
        List<WebElement> optionList;
        int count;
        if (app.equalsIgnoreCase("Advance")) {
            count = 1;
        } else
            count = 2;
        if (option.equalsIgnoreCase("Select All")) {
            optionList = wd.findElements(xpath("//a[contains(@class,'active') and not(text()='Create Upload Files')]"));
            int totOptions = optionList.size();
            String checkedCount = wd.findElement(xpath("(//div[@class='dropdown d-block']//span[@class='caret']/parent::button)[" + count + "]")).getText();
            String[] array = checkedCount.split(" ");
            int chckd = Integer.valueOf(array[0]);
            if (chckd == totOptions) {
                testStepPassed("Successfully validated for selectAll functionality");
                takeScreenshot();
            } else {
                testStepFailed("Failed to validate for selectAll functionality");
            }
        } else {
            optionList = wd.findElements(xpath("//a[contains(@class,'active') and not(text()='Create Upload Files')]"));
            int totOptions = optionList.size();
            if (totOptions == 0) {
                testStepPassed("Successfully validated for DeselectAll functionality");
                takeScreenshot();
            } else {
                testStepFailed("Failed to validate for DeselectAll functionality");
            }
        }
    }

    public static void selectCheckBox(String value) {
        String val = value;
        String[] array = val.split(",");
        int length = array.length;
        for (int i = 0; i < length; i++) {
            if (array[i] != " ") {
                String option = array[i].trim();
                WebElement element = wd.findElement(xpath("//label/parent::div/input[contains(@id,'" + option + "')]"));
                element.click();
                testStepPassed("Successfully clicked on " + option + " checkbox");
            }
        }
        takeScreenshot();
    }

    public static boolean elementToBeInvisible(By by) {
        try {
            WebDriverWait wait = new WebDriverWait(getWebDriver(), 80);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void saveExportedFile(String fileName) {
        try {
            waitTillLoadingCompleted();
            Robot robot = new Robot();
            robot.delay(5000);
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_N);
            robot.delay(1000);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_N);

            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.keyPress(KeyEvent.VK_ENTER);

            robot.delay(3000);
            String path = outputDirectory.replace(".", "").replace("/", "\\");

            boolean file = new File(path + File.separator + "TestDocuments").mkdir();
            String fileCompletePath = path + File.separator + "TestDocuments\\" + fileName;
            robotType(robot, fileCompletePath);
            String lnk_path = ".\\TestDocuments\\" + fileName;
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_N);
            robot.delay(1000);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_N);

            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_ESCAPE);
            if (fileExists(fileCompletePath)) {
                testReporter("blue", "<a href=" + lnk_path + ">View Exported " + fileName + "</a>");


                Path content = Paths.get(fileCompletePath);
                try (InputStream is = Files.newInputStream(content)) {
                    Allure.step("Exported Report Is Here", new Allure.ThrowableContextRunnableVoid<Allure.StepContext>() {
                        @Override
                        public void run(Allure.StepContext context) throws Throwable {
                            Allure.addAttachment("Download Exported Reports For " + fileName, "document/pdf", is, "pdf");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                testStepFailed("File is not downloaded successfully->" + fileName);
            }
        } catch (Exception e) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), e);
        }
    }


    public void saveCrystalReportExportedFile(String fileName) throws AWTException {
        waitTillLoadingCompleted();
        sleep(20000);
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_N);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_N);
        robot.delay(1000);
        robot.keyPress(KeyEvent.VK_TAB);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_TAB);
        robot.delay(1000);
        robot.keyPress(KeyEvent.VK_TAB);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_TAB);
        robot.delay(1000);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_ENTER);
//        robot.keyPress(KeyEvent.VK_DOWN);
        robot.delay(1000);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(5000);
        String path = outputDirectory.replace(".", "").replace("/", "\\");
        boolean file = new File(path + File.separator + "TestDocuments").mkdir();
        String fileCompletePath = path + File.separator + "TestDocuments\\" + fileName;
        robotType(robot, fileCompletePath);
        String lnk_path = ".\\TestDocuments\\" + fileName;
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_N);
        robot.delay(1000);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_N);
        robot.delay(1000);
        robot.keyPress(KeyEvent.VK_ESCAPE);
        if (fileExists(fileCompletePath)) {
            testReporter("blue", "<a href=" + lnk_path + ">View Exported " + fileName + "</a>");
            Path content = Paths.get(fileCompletePath);
            try (InputStream is = Files.newInputStream(content)) {
                Allure.step("Exported Report Is Here", new Allure.ThrowableContextRunnableVoid<Allure.StepContext>() {
                    @Override
                    public void run(Allure.StepContext context) throws Throwable {
                        Allure.addAttachment("Download Exported Reports For " + fileName, "document/pdf", is, "pdf");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            testStepFailed("File is not downloaded successfully->" + fileName);
        }
    }


    public boolean waitForCrystalReport(String object) {
        for (int i = 0; i < 4; i++) {
            waitFor(object);
            if (isElementPresent(object)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isAlertPresent() {
        try {
            wd.switchTo().alert();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    public static boolean waitForAlert() {
        try {
            WebDriverWait wait = new WebDriverWait(ApplicationManager.getWebDriver(), 15);
            wait.until(ExpectedConditions.alertIsPresent());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void handleReportAuth(String UserName) {
        waitForAlert();
        try {
//            if (isAlertPresent()) {
            String target = System.getProperty("target", "local");
            obj.load(new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/local.properties"));
            String pwdDecode = new String(Base64.decodeBase64(obj.getProperty("" + UserName + "")));
            Robot robot = null;
            robot = new Robot();
            robot.delay(2000);
            robotType(robot, UserName);
            robot.delay(3000);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.delay(1000);
            robotType(robot, pwdDecode);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.delay(3000);
            getWebDriver().manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    public static String getIssueDate() {
        String issueDate = "";
        issueDate = wd.findElement(xpath("//input[@name='request_date' and @formcontrolname='issueDttm']")).getAttribute("value");
        return issueDate;
    }

    public static String getMaturityDate() {
        String maturityDate = "";
        maturityDate = wd.findElement(xpath("//input[@name='request_date' and @formcontrolname='maturDttm']")).getAttribute("value");
        return maturityDate;
    }

    public static String getTheBusinessDate() {
        String newDate = "";
        String issueDate = getIssueDate();
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            //issueDate = "10/08/2018";
            cal.setTime(sdf.parse(issueDate));
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                cal.add(Calendar.DATE, 3);
                newDate = sdf.format(cal.getTime());

            } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                cal.add(Calendar.DATE, 2);
                newDate = sdf.format(cal.getTime());
            } else {
                cal.add(Calendar.DATE, 1);
                newDate = sdf.format(cal.getTime());
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return newDate;
    }


    public static String textValue(String object) {
        String text = "";
        try {
            text = wd.findElement(xpath(obj.getProperty(object))).getAttribute("value");
        } catch (Exception e) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), e);
        }
        return text;
    }

    public String gettextValue(WebElement we) {
        try {
            if (isElementPresent(we)) {
                return we.getAttribute("value");
            } else {
                return "";
            }
        } catch (Exception ex) {
            testStepFailed("Unable to get the text element, message is->" + ex.getMessage());
            return "";
        }
    }

    public static void verifyColor(String object, String elemName, String color) {
        String Darkblue = "#0c306d";
        String red = "#ffffff";
        String btncolor = wd.findElement(xpath(obj.getProperty(object))).getCssValue("background-color");
        String hex = Color.fromString(btncolor).asHex().trim();
        switch (color.toLowerCase()) {
            case "red":
                if (hex.equalsIgnoreCase(red)) {
                    testStepPassed(elemName + " button color is red as expected");
                    takeScreenshot();
                } else {
                    testStepFailed(elemName + " button color is not red, Expected->" + red + " but Actual is->" + hex);
                }
                break;
            case "darkblue":
                if (hex.equalsIgnoreCase(Darkblue)) {
                    testStepPassed(elemName + " button color is Dark blue as expected");
                    takeScreenshot();
                } else {
                    testStepFailed(elemName + " button color is not dark blue, Expected->" + red + " but Actual is->" + hex);
                }
                break;
            default:
                testStepFailed("color paramter is not valid");
        }
    }

    public static String DecimalvalueRoundof(String Number) {
        double d = Double.parseDouble(Number);
        DecimalFormat df = new DecimalFormat("############.####");
        df.setRoundingMode(RoundingMode.CEILING);
        String roundedvalue = df.format(d);
        return roundedvalue;
    }

    public static void jsType(String object, String value) {
        try {
            WebElement elem = wd.findElement(xpath(obj.getProperty(object)));
            JavascriptExecutor js = (JavascriptExecutor) wd;
            js.executeScript("arguments[0].setAttribute('value', '" + value + "')", elem);
        } catch (Exception e) {
            testStepFailed("Exception Occured in JsType() " + e.getMessage());
        }
    }


    public static void fileUpload(String filepath) throws AWTException {
        try {
            Alert alert = wd.switchTo().alert();
            alert.sendKeys(filepath);
            alert.accept();
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            waitForAlertDismiss();

        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }

    }


    public static void updateExcelSheet(String filePath, String fileName, String cellAddress, String value) throws AWTException {
        try {
            String worksheet = filePath + fileName + ".xlsx";
            String tempWorksheet = filePath + fileName + "1.xlsx";
            XSSFWorkbook wb = new XSSFWorkbook(new File(worksheet));
            Sheet sh = wb.getSheetAt(1);

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            CellReference cellReference = new CellReference(cellAddress);
            Row row = sh.getRow(cellReference.getRow());
            Cell cell = row.getCell(cellReference.getCol());
            cell.setCellValue(value);
            CellValue cellValue = evaluator.evaluate(cell);
            System.out.println(cellValue);
            FileOutputStream fos = new FileOutputStream(new File(tempWorksheet));
            wb.write(fos);
            fos.close();
            wb.close();
            Files.delete(Paths.get(worksheet));
            new File(tempWorksheet).renameTo(new File(worksheet));

        } catch (Exception ex) {
            testStepFailed("Exception caught while updating the excel" + ex.getMessage());
        }
    }


    public void JsClick(String elem, String elementName) {
        try {
            WebElement weElem = getWebElement(elem);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].click();", weElem);
            testStepInfo("Successfully clicked on element->" + elementName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void JsClick(WebElement elem, String elementName) {
        try {
            sleep(1000);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].click();", elem);
            testStepInfo("Successfully clicked on element->" + elementName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Jsclick(WebElement we, String elementName) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].click();", we);
            testStepInfo("Successfully clicked on element->" + elementName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void validateDeafultElements(String object, String expMsg) {
        String message = getWebElement(object).getText();
        if (message.toLowerCase().contains(expMsg.toLowerCase())) {
            testStepPassed("Default value in fields is:> " + message);
            takeScreenshot();
        } else {
            testStepFailed("Default value: " + expMsg + " is not displayed,instead " + message + " is displayed");

        }

    }


    public void jsClick(String object, String elementName) {
        try {
            sleep(1000);
            WebElement weElem = getWebElement(object);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].click();", weElem);
            testStepInfo("Successfully clicked on element->" + elementName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scroll(String object) {
        WebElement element = getWebElement(object);
        ((JavascriptExecutor) ApplicationManager.getWebDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
        sleep(2000);
    }

    public void clickOnCheckBox(String obj, String value, String elemName) {
        try {
            WebElement we = getWebElement(obj);

            if (value.equals("Y")) {
                if (we.isSelected()) {
                    testStepInfo("elemnet already checked " + elemName);
                } else {
                    we.click();
                    testStepInfo("Clicked on Element-" + elemName);
                }
            } else if (value.equals("N")) {
                if (we.isSelected()) {
                    we.click();
                    testStepInfo("Clicked on Element-" + elemName);
                } else {
                    testStepInfo("Element is unchecked");
                }
            }
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
    }


    public void clickOnCheckBox(WebElement we, String value, String elemName) {
        try {
            if (value.equals("Y")) {
                if (we.isSelected()) {
                    testStepInfo("elemnet already checked " + elemName);
                } else {
                    we.click();
                    testStepInfo("Clicked on Element-" + elemName);
                }
            } else if (value.equals("N")) {
                if (we.isSelected()) {
                    we.click();
                    testStepInfo("Clicked on Element-" + elemName);
                } else {

                }

            }
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
    }

    public boolean verifyisEnabledandisDisabled(String we, String elementname, boolean status) {
        try {
            WebElement webElement = getWebElement(we);
            if (status) {
                if (webElement.isEnabled()) {
                    testStepPassed(elementname + " is enabled as expected");
                    return true;
                } else {
                    testStepFailed(elementname + " is Disabled not working as expected");
                    return false;
                }
            } else {
                if (webElement.isEnabled() == status) {
                    testStepPassed(elementname + " is Disabled as expected");
                    return true;
                } else {
                    testStepFailed(elementname + " is enabled not working as expected");
                    return false;
                }
            }
        } catch (Exception e) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), e);

        }
        return status;
    }


    public static String getFutureDate(int Days) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
        //Getting current date
        Calendar cal = Calendar.getInstance();
        //Displaying current date in the desired format
        System.out.println("Current Date: " + sdf.format(cal.getTime()));

        //Number of Days to add
        cal.add(Calendar.DAY_OF_MONTH, Days);
        //Date after adding the days to the current date
        String newDate = sdf.format(cal.getTime());
        //Displaying the new Date after addition of Days to current date
        return newDate;

    }


    public void selectFromDropdown(WebElement we, int index, String dropdownName) {
        try {
            Select weSel = new Select(we);
            weSel.selectByIndex(index);
            testStepInfo("Selected the value with index " + index + " from the dropdown " + dropdownName);
        } catch (Exception ex) {
            testStepFailed("Unable to select the index " + index + " from dropdown " + dropdownName + " Exception message is->" + ex.getMessage());
        }
    }

    public void selectFromDropdownByValue(WebElement we, String value, String dropdownName) {
        try {
            Select weSel = new Select(we);
            weSel.selectByValue(value);
            testStepInfo("Selected the value " + value + " from the dropdown " + dropdownName);
        } catch (Exception ex) {
            testStepFailed("Unable to select the value " + value + " from dropdown " + dropdownName + " Exception message is->" + ex.getMessage());
        }
    }

    public void getvaluesFromDropdown(WebElement we, String value, String dropdownName) {
        try {
            int count = 0;
            String[] exp = null;
            Select weSel = new Select(we);
            List<WebElement> options = weSel.getOptions();
            for (WebElement we1 : options) {
                for (int i = 0; i < exp.length; i++) {
                    if (we.getText().equals(exp[i])) {
                        count++;
                    }
                }
                testStepInfo("Selected the value " + value + " from the dropdown " + dropdownName);
            }
        } catch (Exception ex) {
            testStepFailed("Unable to select the value " + value + " from dropdown " + dropdownName + " Exception message is->" + ex.getMessage());
        }
    }


    public String getSelectedValueFromDropdown(WebElement we) {
        try {
            Select weSel = new Select(we);
            return weSel.getFirstSelectedOption().getText();
        } catch (Exception ex) {
            testStepFailed("Unable to get the selected value from dropdown , Exception message is->" + ex.getMessage());
            return "";
        }
    }


    public void selectFromDropdown(String select, int index, String dropdownName) {
        try {
            Select weSel = new Select(getWebElement(select));
            weSel.selectByIndex(index);
            testStepInfo("Selected the value with index " + index + " from the dropdown " + dropdownName);
        } catch (Exception ex) {
            testStepFailed("Unable to select the index " + index + " from dropdown " + dropdownName + " Exception message is->" + ex.getMessage());
        }
    }

    public void selectFromDropdownByValue(String select, String value, String dropdownName) {
        try {
            Select weSel = new Select(getWebElement(select));
            weSel.selectByValue(value);
            testStepInfo("Selected the value " + value + " from the dropdown " + dropdownName);
        } catch (Exception ex) {
            testStepFailed("Unable to select the value " + value + " from dropdown " + dropdownName + " Exception message is->" + ex.getMessage());
        }
    }


    public String getSelectedValueFromDropdown(String select) {
        try {
            Select weSel = new Select(getWebElement(select));
            return weSel.getFirstSelectedOption().getText();
        } catch (Exception ex) {
            testStepFailed("Unable to get the selected value from dropdown " + select + ", Exception message is->" + ex.getMessage());
            return "";
        }
    }

    public void Backspace(String object) {
        try {
            WebElement we = getWebElement(object);
            we.click();
            sleep(2000);
            String value = we.getAttribute("value");
            for (int i = 0; i <= value.length(); i++) {
                we.sendKeys(Keys.BACK_SPACE);
            }
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
    }


    public boolean verifyElementDisplayed(String elem, String elemName) {
        try {
            WebElement webElement = getWebElement(elem);
            if (isElementPresent(webElement)) {
                testStepPassed("WebElement " + elemName + " is displayed in the page as expected");
                return true;
            } else {
                testStepFailed("WebElement " + elemName + " is not displayed in the page ");
                return false;
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while verifying the element " + elemName + ", Exception message is->" + ex.getMessage());
            return false;
        }
    }


    public boolean verifyPartialLabelDisplayed(String elementText) {
        try {
            waitTillLoadingCompleted();
            WebElement webElement = getWebElementByPartialText(elementText, "span");
            if (isElementPresent(webElement)) {
                testStepPassed("WebElement with label :" + elementText + " is displayed in the page as expected");
                return true;
            } else {
                testStepFailed("WebElement with label :" + elementText + " is not displayed in the page ");
                return false;
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while verifying the element " + elementText + ", Exception message is-" + ex.getMessage());
            return false;
        }
    }

    public boolean verifyPartialLabelDisplayed(String elementText, String tagName) {
        try {
            sleep(2000);
            WebElement webElement = getWebElementByPartialText(elementText, tagName);
            if (isElementPresent(webElement)) {
                testStepPassed("WebElement with label :" + elementText + " is displayed in the page as expected");
                return true;
            } else {
                testStepFailed("WebElement with label :" + elementText + " is not displayed in the page ");
                return false;
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while verifying the element " + elementText + ", Exception message is-" + ex.getMessage());
            return false;
        }
    }

    public boolean verifyLabelDisplayed(String labelText) {
        try {
            WebElement webElement = getWebElementByText(labelText);
            if (isElementPresent(webElement)) {
                testStepPassed("WebElement with label :" + labelText + " is displayed in the page as expected");
                return true;
            } else {
                testStepFailed("WebElement with label :" + labelText + " is not displayed in the page ");
                return false;
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while verifying the element " + labelText + ", Exception message is->" + ex.getMessage());
            return false;
        }
    }

    public boolean verifyLabelDisplayed(String labelText, String tagName) {
        try {
            //WebElement webElement = ApplicationManager.getWebDriver().findElement(By.xpath("//"+tagName+"[contains(text(),'"+labelText+"')]"));
            WebElement webElement = getWebElementByPartialText(labelText, "h5");
            if (isElementPresent(webElement)) {
                testStepPassed("WebElement with label :" + labelText + " is displayed in the page as expected");
                return true;
            } else {
                testStepFailed("WebElement with label :" + labelText + " is not displayed in the page ");
                return false;
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while verifying the element " + labelText + ", Exception message is->" + ex.getMessage());
            return false;
        }
    }


    public boolean verifyElementNotDsiaplyed(String element, String elementName) {
        try {
            //  WebElement webElement = getWebElement(element);
            if (isElementPresent(element)) {
                testStepFailed("WebElement :" + elementName + " is displayed in the page");
                return false;
            } else {
                testStepPassed("WebElement :" + elementName + " is not displayed in the page as expected");
                return true;
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught while verifying the element :" + elementName + ", Exception message is->" + ex.getMessage());
            return false;
        }
    }




    public void Backspace(WebElement we) {
        try {
            we.click();
            sleep(2000);
            String value = we.getAttribute("value");
            for (int i = 0; i <= value.length() + 2; i++) {
                we.sendKeys(Keys.BACK_SPACE);
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught message is->" + ex.getMessage());
        }
    }


    public void enterText(WebElement we, String text, String elemName) {
        try {
            sleep(2000);
            we.clear();
            if (!(we.getAttribute("value").length() == 0)) {
                Backspace(we);
            }
            we.sendKeys(Keys.HOME);
            sleep(2000);
            we.sendKeys(text);
            sleep(2000);
            testStepInfo("Entered the value in the  :" + elemName + " field :" + text);
        } catch (Exception ex) {
            testStepFailed("Exception caught while entering the value in :" + elemName + " field, message is->" + ex.getMessage());
        }
    }


//    public void enterText(String object, String text, String elemName) {
//        try {
//            WebElement we = getWebElement(object);
//            sleep(3000);
//            we.clear();
//            if (!(we.getAttribute("value").length() == 0)) {
//                Backspace(object);
//            }
//            we.sendKeys(text);
//            testStepInfo("Entered the value in the  :" + elemName + " field :" + text);
//        } catch (Exception ex) {
//            testStepFailed("Exception caught while entering the value in :" + elemName + " field, message is->" + ex.getMessage());
//        }
//    }


    public void enterText(String object, String text, String elemName) {
        try {
            WebElement we = getWebElement(object);
            we.click();
            we.sendKeys(Keys.END);
            we.sendKeys(Keys.CONTROL + "a");
            we.sendKeys(Keys.DELETE);
            we.sendKeys(Keys.HOME);
            we.sendKeys(text);
            sleep(2000);
            testStepInfo("Entered the value in the  :" + elemName + " field :" + text);
        } catch (Exception ex) {
            testStepFailed("Exception caught while entering the value in :" + elemName + " field, message is->" + ex.getMessage());
        }
    }

    public boolean isDisabled(String object, String elementname) {
        try {
            boolean status = true;
            WebElement element = getWebElement(object);
            if (element.isEnabled() == status) {
                testStepFailed(elementname + " is enabled");
                return false;
            } else {
                testStepInfo(elementname + " is disabled as expected ");
                return true;
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
            return false;
        }
    }


    public void enterKEY() {
        Actions action = new Actions(ApplicationManager.driver);
        action.sendKeys(Keys.ENTER).perform();
    }

    public boolean isEnabled(String object, String elementname) {
        try {
            boolean status = true;
            WebElement element = getWebElement(object);
            if (element.isEnabled() == status) {
                testStepInfo(elementname + " is enabled");
                return true;
            } else {
                testStepFailed(elementname + " is not working as expected ");
                return false;
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
            return false;
        }
    }



    public void validateDatabaseValuesDouble(String dbValue, String AppValue, String columnName) {
        if (Double.parseDouble(dbValue) == Double.parseDouble(AppValue)) {
            testStepInfo("Database column " + columnName + " has the expected value -> " + dbValue);
        } else {
            testStepFailed("Database column " + columnName + " does not have expected value, Expected is-> " + AppValue + " But actual is->" + dbValue);
        }
    }

    public void validateDatabaseValuesDate(String dbValue, String AppValue, String columnName) {
        try {
            String[] dbDates = dbValue.split(" ");
            String dbDate = dbDates[0];
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dbFormattedDate = formatter.parse(dbDate);
            SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
            String strDbDate = newFormat.format(dbFormattedDate);
            if (strDbDate.equals(AppValue)) {
                testStepInfo("Database column " + columnName + " has the expected value -> " + strDbDate);
            } else {
                testStepFailed("Database column " + columnName + " does not have expected value, Expected is-> " + AppValue + " But actual is->" + strDbDate);
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught  Message is->" + ex.getMessage());
        }
    }

    public void validateDatabaseValues(String dbValue, String AppValue, String columnName) {
        try {
            if (dbValue.equals(AppValue)) {
                testStepInfo("Database column " + columnName + " has the expected value -> " + dbValue);
            } else {
                testStepFailed("Database column " + columnName + " does not have expected value, Expected is-> " + AppValue + " But actual is->" + dbValue);
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught  Message is->" + ex.getMessage());
        }
    }

    public void validateDatabasevalues(String dbValue, String AppValue, String columnName) {
        try {
            if (dbValue.contains(AppValue)) {
                testStepInfo("Database column " + columnName + " has the expected value -> " + dbValue);
            } else {
                testStepFailed("Database column " + columnName + " does not have expected value, Expected is-> " + AppValue + " But actual is->" + dbValue);
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught  Message is->" + ex.getMessage());
        }
    }

    public void navigateToSubMenu(String strSubMenu) {
        waitTillLoadingCompleted();
        sleep(2000);
        JsClickByVisibleText(strSubMenu);
        //verifyLabelDisplayed(strSubMenu,"h5");
    }

    public static String generaterandomInt(int count) {
        String value = null;
        //Random rand = new Random();
        Random rand = new Random();
        if (count == 1) {
            value = String.valueOf(rand.nextInt(9));
        } else if (count == 2) {
            value = String.valueOf(rand.nextInt(99));
        } else if (count == 3) {
            value = String.valueOf(rand.nextInt(999));
        } else if (count == 4) {
            value = String.valueOf(rand.nextInt(9999));
        } else {
            value = String.valueOf(rand.nextInt(99999));
        }
        return value;

    }


    public void validateErrorMessageForField(WebElement elem, String elemName, String errorMessage) {
        try {
            WebElement we = elem.findElement(By.xpath("./following-sibling::div[1]"));
            if (we.getText().trim().toLowerCase().contains(errorMessage.trim().toLowerCase())) {
                testStepPassed("Error message is displayed in field " + elemName + " as ->" + errorMessage);
            } else {
                testStepFailed("Error message displayed in field " + elemName + " is not valid, Expceted ->" + errorMessage + ", But actual is->" + we.getText());
            }
        } catch (Exception ex) {
            testStepFailed("Error message is not displayed in field " + elemName);
        }
    }

    public void FileUpload(String filepath) throws AWTException {
        try {

            // filepath = " D:\\AutomationFHLBNY\\Sun_SQE\\Phoenix\\Eforms\\validEform3.xlsx";
            //String filepath = " D:\\AutomationFHLBNY\\Sun_SQE\\Phoenix\\eforms\\darshan.txt";
            ClipboardOwner data = null;
            WebDriver wd = getWebDriver();
            Alert alert = wd.switchTo().alert();
            alert.sendKeys(filepath);

            alert.accept();
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            //sleep(3000);
            waitForAlertDismiss();

        } catch (Exception e) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), e);
        }

    }

    public void tabOut() {
        try {
            Robot robot = new Robot();
            robot.delay(3000);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public String getTextValue(WebElement we) {
        try {
            if (isElementPresent(we)) {
                return we.getAttribute("value");
            } else {
                return "";
            }
        } catch (Exception ex) {
            testStepFailed("Unable to get the text element, message is->" + ex.getMessage());
            return "";
        }
    }


    public void deleteData(String object) {
        try {

            WebElement we = getWebElement(object);
            // By locator = By.xpath(obj.getProperty(object));
            we.findElement(By.xpath(object)).sendKeys(Keys.CONTROL);
            we.findElement(By.xpath(object)).sendKeys(Keys.HOME);

            //  we.findElement(locator).sendKeys(Keys.HOME);
            for (int i = 0; i <= 10; i++) {
                // we.findElement(locator).sendKeys(Keys.DELETE);
                we.findElement(By.xpath(object)).sendKeys(Keys.DELETE);

            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }


    public void selectsRecordFromTable(String objTable, int rowNum) {
        WebElement tbl = getWebElement(objTable);
        List<WebElement> weRows = tbl.findElements(By.xpath("./tbody/tr"));
        int rowCount = weRows.size();
        if (rowCount >= rowNum) {
            JsClick(weRows.get(rowNum - 1), rowNum + " row in the table");
        } else {
            testStepFailed("Total row count is only " + rowCount + " , hence row " + rowNum + " is not available");
        }
    }


    public void clear(WebElement we) {
        try {
            we.clear();
        } catch (Exception ex) {

        }
    }


    public String generateRandomAmount() {
        String year = "";
        try {

            Random r = new Random();
            int low = 1000;
            int high = 999999;
            int result = r.nextInt(high - low) + low;
            year = String.valueOf(result);
        } catch (Exception e) {
            testStepFailed("Exception Caught in generateRandomYear()" + e.getMessage());
        }
        return year;
    }


    public int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }


    public void getDatafromfieldsAndEnter(String object) {
        try {
            //By locator = wd.findElement(xpath(obj.getProperty(object)));
            // String existingText = wd.findElement(locator).getAttribute("value");
            String existingText = gettextValue(getWebElement(object));
            if (!existingText.isEmpty()) {


                // wd.findElement(locator).sendKeys(Keys.TAB);
                enterText(object, existingText, "");
                sleep(2000);
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }

    public String buildQuery_RenewLocs_ExpDt(String tableName, HashMap<String, String> record) {
        String query = "Select * from " + tableName + " where ";
        for (Map.Entry<String, String> entry : record.entrySet()) {
            if (!entry.getValue().trim().equals("")) {
                query = query + entry.getKey() + "= '" + entry.getValue() + "' and ";
            }
        }
        if (query.endsWith("and ")) {
            query = query.substring(0, query.length() - 4);
        }
        if (query.endsWith("where ")) {
            query = query.substring(0, query.length() - 6);
        }
        return query;
    }


    public void enterKEY(WebElement we) {
        we.sendKeys(Keys.ENTER);
    }



    public int calculatePgmId(String pgmName) {
        int pgmId = 0;
        switch (pgmName) {
            case "First Home Club": {
                pgmId = 1;
                break;
            }
            case "Homebuyer Dream Program": {
                pgmId = 2;
                break;
            }

            default:
                testStepInfo("Invalid Field Name");

        }
        return pgmId;
    }


    public void selectTheProgramType(String ProgramType) {
        sleep(1000);
        WebElement elem = getWebElement(ProgramType);
        if (ProgramType.equalsIgnoreCase("Homebuyer Dream Program")) {
            elem.click();
            System.out.println("Clicked and now going down element /n");
            elem.sendKeys(Keys.ENTER);
        } else if (ProgramType.equalsIgnoreCase("First Home Club")) {
            System.out.println("Clicking on element/n");
            elem.click();
            System.out.println("Clicked and now going up element/n");
            elem.sendKeys(Keys.UP);
            elem.sendKeys(Keys.ENTER);
        } else if (ProgramType.equalsIgnoreCase("None")) {
            testStepInfo("Data is not hidden in both set-aside program");
        }
    }


    public void validateViolationErrorMessage(String object, String expMessage, String flagObject, String ExpectedFlag) {
        try {
            String message = getWebElement(object).getText();
            if (message.toLowerCase().contains(expMessage.toLowerCase())) {
                testStepPassed("Expected message is Displayed ->" + message);
                takeScreenshot();
            } else {
                testStepFailed("Expected Message->" + expMessage + " is not displayed,instead " + message + " is displayed");

            }
            String flag = getWebElement(flagObject).getText();
            if (flag.toLowerCase().contains(ExpectedFlag.toLowerCase())) {
                testStepPassed("Expected Flag is Displayed ->" + flag);
                takeScreenshot();
            } else {
                testStepFailed("Expected Flag->" + ExpectedFlag + " is not displayed,instead " + flag + " is displayed");

            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }

    }

    public String getValueFromAnExcel(String filePath, String sheetName, int rowNo, String cellReference) throws InterruptedException {
        String cellValue = "";
        try {
            //Read the spreadsheet that needs to be updated
            FileInputStream fsIP = new FileInputStream(new File(filePath));

//Access the workbook
            Workbook wb = WorkbookFactory.create(fsIP);
//Access the worksheet, so that we can update / modify it.
            Sheet worksheet = wb.getSheet(sheetName);
            Row r = worksheet.getRow(rowNo);
// declare a Cell object
            Cell cell = null;
// Access the second cell in second row to update the value
            cell = r.getCell(CellReference.convertColStringToIndex(cellReference));
            System.out.println("Cell\t" + cell);
            cellValue = cell.getStringCellValue();
//Close the InputStream
            fsIP.close();
            //close the workbook
            wb.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return cellValue;
    }


//    public String getNextBusinessDate(String date) {
//        String nextBusineesDate = "";
//        Date dates1 = java.sql.Date.valueOf("2/02/2020");
//            SimpleDateFormat newFormate = new SimpleDateFormat("MM/dd/yyyy");
//            String nextBusineesDate1 = newFormate.format(dates1);
//            LocalDate convertedDate1 = LocalDate.parse(date, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
//            LocalDate nextDate = convertedDate1.minusDays(Integer.parseInt(getRandomNumbericString(1)));
//            Date nextDateFormatted = Date.from(nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//            String[] dayname = nextDateFormatted.toString().split(" ");
//            if (dayname[0].equals("Sat")) {
//                nextDate = nextDate.plusDays(2);
//                Date dates = java.sql.Date.valueOf(nextDate);
//                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
//                nextBusineesDate = newFormat.format(dates);
//            } else if (dayname[0].equals("Sun")) {
//                // c.add(Calendar.DAY_OF_MONTH, 7);
//                nextDate = nextDate.plusDays(1);
//                Date dates = java.sql.Date.valueOf(nextDate);
//                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
//                nextBusineesDate = newFormat.format(dates);
//            } else {
//                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
//                Date dates = java.sql.Date.valueOf(nextDate);
//                nextBusineesDate = newFormat.format(dates);
//            }
//        } catch (Exception ex) {
//            testStepFailed("Exception caught  Message is->" + ex.getMessage());
//        }
//        return nextBusineesDate;
//    }


    /*  LocalDate convertedDate1;
      public String getNextBusinessDate(String date) {
          String nextBusineesDate = "";
          try{
              LocalDate date1 =date;
              DateTimeFormatter formatters = DateTimeFormatter.ofPattern("d/MM/uuuu");
              String text = date1.format(formatters);
              LocalDate parsedDate = LocalDate.parse(text, formatters);
              LocalDate date = LocalDate.now();
              DateTimeFormatter formatters = DateTimeFormatter.ofPattern("d/MM/uuuu");
              String text = date.format(formatters);
              LocalDate parsedDate = LocalDate.parse(text, formatters);
              LocalDate convertedDate1 = LocalDate.(date, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
              DateTimeFormatter formatters1 = DateTimeFormatter.ofPattern("d/MM/uuuu");
              String text = convertedDate1.format(formatters1);
              LocalDate convertedDate1 = LocalDate.parse(date, DateTimeFormatter.ofPattern("MM/dd/yyyy"));

              LocalDate nextDate = convertedDate1.plusDays(1);
              Date nextDateFormatted = Date.from(nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
              String[] dayname = nextDateFormatted.toString().split(" ");
              if (dayname[0].equals("Sat")) {
                  nextDate = nextDate.plusDays(2);
                  Date dates = java.sql.Date.valueOf(nextDate);
                  SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                  nextBusineesDate = newFormat.format(dates);
              } else if (dayname[0].equals("Sun")) {
                  // c.add(Calendar.DAY_OF_MONTH, 7);
                  nextDate = nextDate.plusDays(1);
                  Date dates = java.sql.Date.valueOf(nextDate);
                  SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                  nextBusineesDate = newFormat.format(dates);
              } else {
                  SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                  Date dates = java.sql.Date.valueOf(nextDate);
                  nextBusineesDate = newFormat.format(dates);
              }
          } catch (Exception ex) {
              testStepFailed("Exception caught  Message is->" + ex.getMessage());
          }
          return nextBusineesDate;
      }
  */
    public String getNextBusinessDate(String date) {
        String nextBusineesDate = "";
        try {
            LocalDate convertedDate1 = LocalDate.parse(date, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            LocalDate nextDate = convertedDate1.plusDays(1);
            Date nextDateFormatted = Date.from(nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            String[] dayname = nextDateFormatted.toString().split(" ");
            if (dayname[0].equals("Sat")) {
                nextDate = nextDate.plusDays(2);
                Date dates = java.sql.Date.valueOf(nextDate);
                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                nextBusineesDate = newFormat.format(dates);
            } else if (dayname[0].equals("Sun")) {
                // c.add(Calendar.DAY_OF_MONTH, 7);
                nextDate = nextDate.plusDays(1);
                Date dates = java.sql.Date.valueOf(nextDate);
                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                nextBusineesDate = newFormat.format(dates);
            } else {
                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                Date dates = java.sql.Date.valueOf(nextDate);
                nextBusineesDate = newFormat.format(dates);
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught  Message is->" + ex.getMessage());
        }
        return nextBusineesDate;
    }

    public String getTheBusinessDate(String date) {
        String newDate = "";

        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            cal.setTime(sdf.parse(date));

            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DATE, 2);
                newDate = sdf.format(cal.getTime());

            } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                cal.add(Calendar.DATE, 3);
                newDate = sdf.format(cal.getTime());
            } else {
                cal.add(Calendar.DATE, 1);
                newDate = sdf.format(cal.getTime());
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return newDate;
    }


    public void validateNoErrorMessageForField(WebElement elem, String elemName) {
        try {
            List<WebElement> wes = elem.findElements(By.xpath("./following-sibling::div/div[1]"));
            if (wes.size() == 0) {
                testStepPassed("Error message is not displayed in field " + elemName + " as expected");
            } else {
                testStepFailed("Error message displayed in field " + elemName + " ,Message is ->" + wes.get(0).getText());
            }
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
    }

    public void validateNoErrorMessageForFields(WebElement elem, String elemName, String errorMessage) {
        try {
            sleep(2000);


            if ( elem.getText().length()== 0  ) {
                testStepPassed("Error message is displayed in field " + elemName + " as ->" + errorMessage);
            } else {
                testStepFailed("Error message displayed in field " + elemName + " is not valid, Expceted ->" + errorMessage + ", But actual is->" + elem.getText());
            }
        } catch (Exception ex) {
            testStepFailed("Error message is not displayed in field " + elemName);
        }
    }

    public void validateAllfieldsInDropdown(String object, List<String> ExpectedOptions, String SubMenu) {

        try {

            WebElement we = getWebElement(object);
            int count = 0;
//            Select weSel = new Select(we);
            List<WebElement> options = we.findElements(By.tagName("li"));
            for (WebElement li : options) {
                if (ExpectedOptions.contains(li.getText())) {
                    testStepInfo("Menu option " + SubMenu + " contains all the options present");
                } else {
                    testStepFailed("Options do not match in " + SubMenu + ".");
                }
            }
        } catch (Exception ex) {
            testStepFailed("Unable to find Menu Option " + SubMenu + " Exception message is-> " + ex.getMessage());
        }

    }

    public List<String> returnTextBoxElementList(String object, String page, int txtBoxtotal) {



        WebElement we = getWebElement(object);
        List<String> txt = new ArrayList<String>();
        List<WebElement> txtBox = we.findElements(By.tagName("input"));
        List<WebElement> list = we.findElements(By.tagName("label"));
        for (WebElement li : list) {

            txt.add(li.getText());

        }

        if (txtBox.size() == txtBoxtotal) {

            testStepPassed("All the test boxes are present in the " + page);

        } else {

            testStepFailed("Textbox missing on the " + page);

        }

        return txt;

    }

    public List<String> getTableRow(String table, int record) {

        WebElement tbl = getWebElement(table);
        List<WebElement> weRows = tbl.findElements(By.xpath("./tbody/tr"));
        List<WebElement> columns = weRows.get(record).findElements(By.tagName("td"));
        List<String> values = new ArrayList<String>();
        for (WebElement col : columns) {

            values.add(col.getText());

        }

        return values;

    }

    public void validateAllRecordsInTextbox(String object, List<String> record, String page) {

        WebElement pageTxtBox = getWebElement(object);
        List<WebElement> txtBox = pageTxtBox.findElements(By.tagName("input"));
        List<String> values = new ArrayList<String>();
        int i = 0;
        for (WebElement txt : txtBox) {

            System.out.println(i + " : " + txt.getAttribute("value"));
            values.add(txt.getText());

        }

        System.out.println("List table: " + record);
        System.out.println("List textBox: " + txtBox);

        if (record.equals(values)) {

            testStepPassed("All the values match as per " + page + " Table");

        }

    }

    public void searchCompare(String expected, String actual, String searchingFor) {
        System.out.println("expected : " + expected + "\tactual: " + actual);
        if (actual.contains(expected)) {

            testStepPassed("Comparison for " + searchingFor + "successful");

        } else {

            testStepFailed("Search result do not contain value of " + searchingFor);
        }

    }

    public void valueCompare(String expected, String actual, String searchingFor) {

        System.out.println(searchingFor + ":\texpected: " + expected + "\tactual :" + actual);
        if (actual.trim().equalsIgnoreCase(expected.trim())) {

            testStepInfo("Comparison for " + searchingFor + " successful");

        } else {

            testStepFailed("Value do not match for " + searchingFor);
        }

    }

    public int extractRowCount(String recordEntries) {

        String rowcount = getTextValue(recordEntries);
        String[] subRecords = rowcount.split("of ");
        String num = subRecords[1];
        num = num.replaceAll("entries", "");
        num = num.trim();
        System.out.println("Rows: " + num);
        return Integer.parseInt(num);

    }


    public void validateSubMenuListed(String MainMenu, List<String> subMenus) {
        try {
            boolean blnTrue = true;
            WebElement weMainMenu = getWebElementByPartialText(MainMenu);
            if (isElementPresent(weMainMenu)) {
                clickOn(weMainMenu, MainMenu + "Menu Link");
                for (String strSubMenu : subMenus) {
                    WebElement weSubMenu = getWebElementByPartialText(strSubMenu);
                    if (isElementPresent(weSubMenu)) {
                        testStepInfo("Sub Menu Item " + strSubMenu + " is displayed under the Main Menu " + MainMenu);
                    } else {
                        blnTrue = false;
                        testStepFailed("Unable to find the Sub Menu Item " + strSubMenu + " under the Main Menu " + MainMenu);
                    }
                }
                if (blnTrue) {
                    testStepInfo("All the Sub Menu Items are displayed under the Main Menu " + MainMenu);
                }
            } else {
                testStepFailed("Unable to find the Menu Item " + MainMenu);
            }
        } catch (Exception e) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), e);
        }
    }

    public void validatesExportFunctionality() {
        try {
            sleep(5000);
            String path = outputDirectory.replace(".", "").replace("/", "\\");
            boolean file = new File(path + File.separator + "TestDocuments").mkdir();
            String strFolderPath = path + File.separator + "TestDocuments\\";
            int beforeCount = getFileCount(strFolderPath);

            String saveFile = getFileName();
            sleep(10000);
            verifyAndAttachReport(beforeCount);
        } catch (Exception ex) {
            testStepFailed("Exception caught while validating the generic elements in page, Message is->" + ex.getMessage());
        }
    }

    public int getFileCount(String FolderPath) {
        try {
            return new File(FolderPath).listFiles().length;
        } catch (Exception ex) {
            return 0;
        }
    }

    public boolean verifyAndAttachReport(int beforeCount) {
        try {
            sleep(5000);
            String path = outputDirectory.replace(".", "").replace("/", "\\");
            boolean file = new File(path + File.separator + "TestDocuments").mkdir();
            String strFolderPath = path + File.separator + "TestDocuments\\";

            int afterCount = getFileCount(strFolderPath);
            if (beforeCount == afterCount) {
                File latestFile = lastFileModified(strFolderPath);
                String latestFileName = latestFile.getCanonicalFile().getName();
                String lnk_path = ".\\TestDocuments\\" + latestFileName;
                testReporter("blue", "<a href='" + lnk_path + "'>View Exported Report</a>");
                return true;
            } else {
                testStepFailed("File is not downloaded successfully->");
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public File lastFileModified(String dir) {
        File fl = new File(dir);
        File[] files = fl.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }

    public void validateErrorMessageForFields(WebElement elem, String elemName, String errorMessage) {
        try {
            sleep(2000);
            if (elem.getText().trim().toLowerCase().equals(errorMessage.trim().toLowerCase())) {
                testStepPassed("Error message is displayed in field " + elemName + " as ->" + errorMessage);
            } else {
                testStepFailed("Error message displayed in field " + elemName + " is not valid, Expceted ->" + errorMessage + ", But actual is->" + elem.getText());
            }
        } catch (Exception ex) {
            testStepFailed("Error message is not displayed in field " + elemName);
        }
    }


    public String getSelectedValueFromDropdownWithNull(String select) {
        try {
            Select weSel = new Select(getWebElement(select));
            if (!weSel.getFirstSelectedOption().getText().equals("Select State"))
                return weSel.getFirstSelectedOption().getText();
            else
                return "";
        } catch (Exception ex) {
            return "";
        }
    }

    public int noOfRecords(String object) {

        WebElement tbl = getWebElement(object);
        List<WebElement> weRows = tbl.findElements(By.xpath("./tbody/tr"));
        int count = weRows.size();
        return count;

    }


    public void validatedrpdwn(String select, List<String> record, String page) {

        Select weSel = new Select(getWebElement(select));

        List<WebElement> txtBox = weSel.getOptions();

        List<String> values = new ArrayList<String>();
        int i = 0;
        for (WebElement txt : txtBox) {

            System.out.println(i + " : " + txt.getAttribute("value"));
            values.add(txt.getText());

        }

        if (record.equals(values)) {

            testStepPassed("All the values match as per " + page + " Table and options are" + values);

        }

    }

    public void navigateBack() {

        getWebDriver().navigate().back();

    }

    public void compareFloatValue(String expected, String actual, String searchingFor) {

        try {

            System.out.println(searchingFor + ":\texpected: " + expected + "\tactual :" + actual);
            Float expFloat = Float.parseFloat(expected);
            Float actFloat = Float.parseFloat(actual);
            if (Float.compare(expFloat, actFloat) == 0) {

                testStepPassed("Comparison for " + searchingFor + " successful");

            } else {
                testStepFailed("Value do not match for " + searchingFor);
            }

        } catch (NumberFormatException e) {

            valueCompare(expected, actual, searchingFor);

        }

    }

    public void validateDropdowwnvalues(String dbValue, String AppValue, String columnName) {
        try {
            if (dbValue.contains(AppValue)) {
                testStepInfo("Database column " + columnName + " has the expected value -> " + AppValue);
            } else {
                testStepFailed("Database column " + columnName + " does not have expected value, Expected is-> " + AppValue);
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught  Message is->" + ex.getMessage());
        }
    }

    public String getPrevBusinessDate(String date) {
        String newDate = "";

        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            cal.setTime(sdf.parse(date));

            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DATE, -2);
                newDate = sdf.format(cal.getTime());

            } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                cal.add(Calendar.DATE, -3);
                newDate = sdf.format(cal.getTime());
            } else {
                cal.add(Calendar.DATE, -1);
                newDate = sdf.format(cal.getTime());
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return newDate;
    }

    public String getNonBusinessDate(String date) {
        String newDate = "";

        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            cal.setTime(sdf.parse(date));

            int days = Calendar.SUNDAY - cal.get(Calendar.DAY_OF_WEEK);
            if (days < 0) {
                days += 7;
            }

            cal.add(Calendar.DAY_OF_YEAR, days);
            newDate = sdf.format(cal.getTime());

        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return newDate;
    }


    public void verifyCheckBoxIsSelectedOrNot(String obj, String value, String elemName) {
        try {
            WebElement we = getWebElement(obj);

            if (value.equals("Y")) {
                if (we.isSelected()) {
                    testStepInfo("element is checked " + elemName);
                } else {
                    testStepFailed("element is not checked -" + elemName);
                }
            } else if (value.equals("N")) {
                if (!we.isSelected()) {
                    testStepInfo("element is not checked " + elemName);

                } else {
                    testStepFailed("element is checked -" + elemName);
                }
            }
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
    }


    public void validatesErrorMessageForField(WebElement elem, String elemName, String errorMessage) {
        try {
            WebElement we = elem.findElement(By.xpath("./following-sibling::div/div[1]"));
            if (we.getText().trim().toLowerCase().contains(errorMessage.trim().toLowerCase())) {
                testStepPassed("Error message is displayed in field " + elemName + " as ->" + errorMessage);
            } else {
                testStepFailed("Error message displayed in field " + elemName + " is not valid, Expceted ->" + errorMessage + ", But actual is->" + we.getText());
            }
        } catch (Exception ex) {
            testStepFailed("Error message is not displayed in field " + elemName);
        }
    }


    public void validateExportFunctionality1(String format) {
        WebDriver wd = getWebDriver();

        String saveFile = wd.getTitle() + getRandomNumbericString(2);
        try {
            if (format.equals("XLS")) {
                String fileName = saveFile + " Report.xls";
                if (browserType().contains("chrome"))
                    savesExportedFile(fileName);
                else
                    saveCrystalReportExportedFile(fileName);
            }
            if (format.equals("CSV")) {
                String fileName = saveFile + " Report.csv";
                if (browserType().contains("chrome"))
                    savesExportedFile(fileName);
                else
                    saveCrystalReportExportedFile(fileName);
            }
            if (format.equals("PDF")) {
                String fileName = saveFile + " Report.pdf";
                if (browserType().contains("chrome"))
                    savesExportedFile(fileName);
                else
                    saveCrystalReportExportedFile(fileName);
            }
        } catch (Exception e) {
            testStepFailed("Failed due to exception: " + e);
        }
    }


    public void savesExportedFile(String fileName) {
        try {
            waitTillLoadingCompleted();
            Robot robot = new Robot();
            robot.delay(5000);
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_N);
            robot.delay(1000);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_N);

            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.keyPress(KeyEvent.VK_ENTER);


            robot.delay(3000);
            String path = outputDirectory.replace(".", "").replace("/", "\\");

            boolean file = new File(path + File.separator + "TestDocuments").mkdir();
            String fileCompletePath = path + File.separator + "TestDocuments\\" + fileName;
            robotType(robot, fileCompletePath);
            String lnk_path = ".\\TestDocuments\\" + fileName;
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_N);
            robot.delay(1000);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_N);


            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_ESCAPE);
            if (fileExists(fileCompletePath)) {
                testReporter("blue", "<a href=" + lnk_path + ">View Exported " + fileName + "</a>");


                Path content = Paths.get(fileCompletePath);
                try (InputStream is = Files.newInputStream(content)) {
                    Allure.step("Exported Report Is Here", new Allure.ThrowableContextRunnableVoid<Allure.StepContext>() {
                        @Override
                        public void run(Allure.StepContext context) throws Throwable {
                            Allure.addAttachment("Download Exported Reports For " + fileName, "document/pdf", is, "pdf");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                testStepFailed("File is not downloaded successfully->" + fileName);
            }
        } catch (Exception e) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), e);
        }
    }


    public void validateExportFunctionality(String saveFile, String format) {
        try {

            if (format.equals("XLS")) {
                String fileName = saveFile + " Report.xls";
                if (browserType().contains("chrome"))
                    saveExportedFile(fileName);
                else
                    saveCrystalReportExportedFile(fileName);
            }

            if (format.equals("PDF")) {
                String fileName = saveFile + " Report.pdf";
                if (browserType().contains("chrome"))
                    saveExportedFile(fileName);
                else
                    saveCrystalReportExportedFile(fileName);

            }
        } catch (Exception e) {
            testStepFailed("Failed due to exception: " + e);

        }
    }


    public void validatesExportFunctionality(String format) {
        WebDriver wd = getWebDriver();

        String saveFile = wd.getTitle();

        if (format.equals("XLS")) {
            String fileName = saveFile + " Report.xls";
            saveExportedFile(fileName);
        }
        if (format.equals("CSV")) {
            String fileName = saveFile + " Report.csv";
            saveExportedFile(fileName);
        }
        if (format.equals("PDF")) {
            String fileName = saveFile + " Report.pdf";
            saveExportedFile(fileName);
        }
    }

    public List<String> getValueFromDropdown(String select) {

        Select weSel = new Select(getWebElement(select));

        List<WebElement> txtBox = weSel.getOptions();

        List<String> values = new ArrayList<String>();
        int i = 0;
        for (WebElement txt : txtBox) {

            System.out.println(i + " : " + txt.getText());
            values.add(txt.getText().trim());

        }

        return values;

    }

    public String removeSpecialChar(String num) {

        String updatedNumber = num.replaceAll("-", "");
        updatedNumber = updatedNumber.replaceAll(",", "");
        updatedNumber = updatedNumber.replaceAll("[\\[\\](){}]", "");
        updatedNumber = updatedNumber.replaceAll("\\s", "");
        return updatedNumber;

    }

    public String addDaysToGivenDate(String date, String days) {
        String newDate = "";

        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            cal.setTime(sdf.parse(date));

            int intDate = Integer.parseInt(days);

            cal.add(Calendar.DATE, intDate);
//            cal.add(Calendar.DAY_OF_MONTH, intDate);
            newDate = sdf.format(cal.getTime());
            newDate = getTheBusinessDate(newDate);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return newDate;
    }

    public void jsEnterValue(String elem, String value, String elementName) {
        try {
            WebElement weElem = getWebElement(elem);
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].value='" + value + "';", weElem);
            testStepInfo("Successfully entered value: " + value + " on element->" + elementName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPage() {

        getWebDriver().navigate().refresh();

    }

    /**
     * to check presence of the given attribute
     */
    public boolean isAttributePresent(String object, String atrributeName) {
        try {
            WebElement element = getWebElement(object);
            String value = element.getAttribute(atrributeName);
            if (value != null) {
                testStepInfo(atrributeName + " attribute is present as expected");
                return true;
            } else {
                testStepFailed(atrributeName + " attribute is not present");
                return false;
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
            return false;
        }
    }

    /**
     * for validating whether elements are disabled on Commitment page
     */
    public boolean isDisabled1(String object, String atrributeName, String elementname) {
        try {
            if (isAttributePresent(object, atrributeName)) {
                testStepInfo(elementname + " is disabled as expected ");
                return true;
            } else {
                testStepFailed(elementname + " is enabled");
                return false;
            }
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
            return false;
        }
    }

    public int getSizeDrpDownOptions(String objectSelect) {
        try {
            return getSelectedOptionFromdropdown(getWebElement(objectSelect)).size() - 1;
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
            return 0;
        }
    }

    public boolean isEnabled(WebElement we) {
        try {
            we.isEnabled();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isSelected(String obj, String elemName) {
        try {
            WebElement we = getWebElement(obj);
            if (we.isSelected()) {
                testStepInfo("element is checked " + elemName);
                return true;
            } else {
                testStepFailed("element is not checked -" + elemName);
                return false;
            }
        } catch (NoSuchElementException e) {
            return false;
        }
    }


    public boolean isInputFieldEmpty(String obj, String elemName) {
        try {

            if (gettext(obj).equalsIgnoreCase("")) {
                testStepInfo("element is empty->" + elemName);
                return true;
            } else {
                testStepFailed("element is empty ->" + elemName);
                return false;
            }
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isStringsEqual(String str1, String obj) {
        try {

            if (str1.trim().equalsIgnoreCase(gettext(obj).trim())) {
                testStepInfo("Both String values are same->" + str1);
                return true;
            } else {
                testStepFailed("Both Strings are not equal. str1->" + str1 + " str2->" + gettext(obj));
                return false;
            }
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void clear1(String object) {
        try {
            WebElement we = getWebElement(object);
            we.clear();
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }

    public void resizeBrowser() {
        try {
            Dimension oDimension = new Dimension(480, 620);
            getWebDriver().manage().window().setSize(oDimension);
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
        }
    }

    public List<String> getOptionsFromDropDown(String selectObj) {
        try {
            Select oSelect = new Select(getWebElement(selectObj));
            int i = 0;
            List<String> oList = new ArrayList<>();
            for (WebElement we : oSelect.getOptions()) {
                if (i != 0) {
                    String str = we.getText().trim();
                    oList.add(str);
                }
                i++;
            }
            return oList;
        } catch (Exception e) {
            String method = new Exception().getStackTrace()[0].getMethodName();
            testStepFailed("Exception Caught in method " + method + ", and Error Message is->" + e.getMessage());
            return null;
        }
    }



    public void waitTillSpinnerDisappears() {

        WebDriverWait wait = new WebDriverWait(getWebDriver(), 20);
        wait.until(ExpectedConditions.visibilityOf(getWebDriver().findElement(By.xpath("//*[contains(@class,'spinner')]"))));
        wait.until(ExpectedConditions.invisibilityOf(getWebDriver().findElement(By.xpath("//*[contains(@class,'spinner')]"))));

    }

    public void enterDateInCalender(String date) {

        WebElement calender = getWebElement(".//ngb-datepicker[@class='dropdown-menu show ng-star-inserted']");
        WebElement selMonth = calender.findElement(By.xpath("//select[@title='Select month']"));
        WebElement selYear = calender.findElement(By.xpath("//select[@title='Select year']"));
        System.out.println("Year: " + date.substring(6, 10) + "\nDate: " + date.substring(3, 5) + "\nMonth: " + date.substring(0, 2));
        selectFromDropdownByValue(selYear, date.substring(6, 10), "Year dropdown");
        if (date.startsWith("0"))
            selectFromDropdownByValue(selMonth, date.substring(1, 2), "Month dropdown");
        else
            selectFromDropdownByValue(selMonth, date.substring(0, 2), "Month dropdown");
        if (date.startsWith("0", 3))
            calender.findElement(By.xpath(".//ngb-datepicker-month-view[@role='grid']//div[@class='btn-light ng-star-inserted' and text()='" + date.substring(4, 5) + "']")).click();
        else
            calender.findElement(By.xpath(".//ngb-datepicker-month-view[@role='grid']//div[@class='btn-light ng-star-inserted' and text()='" + date.substring(3, 5) + "']")).click();

    }

    public void EnterKey() {
        try {
            Robot robot = new Robot();
            robot.delay(3000);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public String getBusinessDate(String date, int n) {
        String nextBusineesDate = "";
        try {
            LocalDate convertedDate1 = LocalDate.parse(date, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            LocalDate nextDate = convertedDate1.plusDays(n);
            Date nextDateFormatted = Date.from(nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            String[] dayname = nextDateFormatted.toString().split(" ");
            if (dayname[0].equals("Sat")) {
                nextDate = nextDate.plusDays(2);
                Date dates = java.sql.Date.valueOf(nextDate);
                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                nextBusineesDate = newFormat.format(dates);
            } else if (dayname[0].equals("Sun")) {
                // c.add(Calendar.DAY_OF_MONTH, 7);
                nextDate = nextDate.plusDays(1);
                Date dates = java.sql.Date.valueOf(nextDate);
                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                nextBusineesDate = newFormat.format(dates);
            } else {
                SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
                Date dates = java.sql.Date.valueOf(nextDate);
                nextBusineesDate = newFormat.format(dates);
            }
        } catch (Exception ex) {
            testStepFailed("Exception caught  Message is->" + ex.getMessage());
        }
        return nextBusineesDate;
    }

    public void valueCompares(String expected, String actual, String searchingFor) {

        System.out.println(searchingFor + ":\texpected: " + expected + "\tactual :" + actual);
        if (actual.trim().contains(expected.trim())) {

            testStepInfo("Comparison for " + searchingFor + " successful");

        } else {

            testStepFailed("Value do not match for " + searchingFor);
        }

    }

    public void entertext(String object, String text, String elemName) {
        try {
            WebElement we = getWebElement(object);
            sleep(3000);
            we.clear();
            if (!(we.getAttribute("value").length() == 0)) {
                Backspace(object);
            }
            we.sendKeys(text);
            testStepInfo("Entered the value in the  :" + elemName + " field :" + text);
        } catch (Exception ex) {
            testStepFailed("Exception caught while entering the value in :" + elemName + " field, message is->" + ex.getMessage());
        }
    }


    public void opensReport(String type) {
        try {
            if (type.equalsIgnoreCase("PDF")) {
                Robot robot = new Robot();


                robot.delay(5000);
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_N);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_N);
                robot.delay(1000);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(1000);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(1000);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.delay(3000);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_W);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyRelease(KeyEvent.VK_SHIFT);
                robot.keyRelease(KeyEvent.VK_W);
                robot.delay(5000);
               /* robot.keyRelease(KeyEvent.VK_ALT);
                robot.delay(2000);
*/   //temp
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(1000);

            } else {
                Robot robot = new Robot();
                robot.delay(5000);
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_N);
                robot.delay(2000);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_N);
                robot.delay(2000);
                robot.keyPress(KeyEvent.VK_TAB);
//                robot.delay(1000);
                //temp
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(2000);

                robot.keyPress(KeyEvent.VK_TAB);
//                robot.delay(1000);
                //temp
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(2000);


                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);

                robot.delay(3000);

                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyPress(KeyEvent.VK_C);
                robot.delay(5000);
                //temp
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_SPACE);
                robot.keyRelease(KeyEvent.VK_C);
                robot.delay(2000);


            }
        } catch (Exception ex) {
            ex.getStackTrace();


        }
    }


    public void openReport(String type) {
        try {
            if (type.equalsIgnoreCase("PDF")) {
                Robot robot = new Robot();

                robot.delay(5000);
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_N);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_N);
                robot.delay(1000);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(1000);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(1000);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.delay(3000);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_W);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyRelease(KeyEvent.VK_SHIFT);
                robot.keyRelease(KeyEvent.VK_W);
                robot.delay(5000);

            } else {
                Robot robot = new Robot();

                robot.delay(5000);
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_N);
                robot.delay(1000);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_N);
                robot.delay(1000);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.delay(1000);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.delay(3000);


                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyPress(KeyEvent.VK_C);
                robot.delay(5000);
                //temp
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_SPACE);
                robot.keyRelease(KeyEvent.VK_C);
                robot.delay(2000);

            }
        } catch (Exception ex) {
            ex.getStackTrace();


        }
    }

    public boolean verifyAndAttachReport(int beforeCount, String format) throws IOException {
        try {
            String path = outputDirectory.replace(".", "").replace("/", "\\");
            boolean file = new File(path + File.separator + "TestDocuments").mkdir();
            String strFolderPath = path + File.separator + "TestDocuments\\";
            sleep(2000);
            int afterCount = getFileCount(strFolderPath);
            if (beforeCount < afterCount) {
                File latestFile = lastFileModified(strFolderPath);
                String latestFileName = latestFile.getCanonicalFile().getName();
                String lnk_path = ".\\TestDocuments\\" + latestFileName;
                String Filecompltepath = path + File.separator + "TestDocuments\\" + latestFileName;

                //  Path content = Paths.get(strFolderPath);
                if (fileExists(Filecompltepath)) {
                    testReporter("blue", "<a href=" + lnk_path + ">View Exported " + latestFileName + "</a>");

                    Path content = Paths.get(Filecompltepath);
                    try (InputStream is = Files.newInputStream(content)) {
                        Allure.step("Exported Report Is Here", new Allure.ThrowableContextRunnableVoid<Allure.StepContext>() {
                            @Override
                            public void run(Allure.StepContext context) throws Throwable {
                                Allure.addAttachment("Download Exported Reports For " + lnk_path, "document", is, format);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    testStepFailed("File is not downloaded successfully->" + lnk_path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String browserType() {

        return reader.get("browser.type").toLowerCase();

    }

    public String addDaysToDate(String strDate, int intDays) {
        String newDate = "";
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            //strDate = "10/08/2018";
            cal.setTime(sdf.parse(strDate));

            cal.add(Calendar.DATE, intDays);
            newDate = sdf.format(cal.getTime());
            newDate = getTheBusinessDate(newDate);


        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return newDate;
    }

    public String roundUpToTwoDecimal(String strFloatValue) {
        String strRoundedValue = "";
        try {
            BigDecimal bd = new BigDecimal(strFloatValue).setScale(2, RoundingMode.HALF_UP);
            return strRoundedValue = bd.toString();

        } catch (Exception ex) {
            testStepFailed("Exception caught  Message is->" + ex.getMessage());
        }
        return strRoundedValue;
    }


    public String convertDtFormatToEST(String dbValue) {
        String strDate = "";
        try {
            String[] dbDates = dbValue.split(" ");
            String dbDate = dbDates[0];
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dbFormattedDate = formatter.parse(dbDate);
            SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
            strDate = newFormat.format(dbFormattedDate);

        } catch (Exception ex) {
            testStepFailed("Exception caught  Message is->" + ex.getMessage());
        }
        return strDate;
    }

    public String buildQueryForTopTenRenewLocs(String tableName, HashMap<String, String> record) {
        String query = "Select Top 10 * from " + tableName + " where ";
        for (Map.Entry<String, String> entry : record.entrySet()) {
            if (!entry.getValue().trim().equals("")) {
                query = query + entry.getKey() + "= '" + entry.getValue() + "' and ";
            }
        }
        if (query.endsWith("and ")) {
            query = query.substring(0, query.length() - 4);
        }
        if (query.endsWith("where ")) {
            query = query.substring(0, query.length() - 6);
        }
        return query + " and commt_expire_dttm >(select cm_prev_update_dttm from system_cm_control)";
    }


    public String buildQueryForTopTenRenewLocs_MultipleFliter(String tableName, HashMap<String, String> record) {
        String query = "Select Top 10 * from " + tableName + " where ";
        for (Map.Entry<String, String> entry : record.entrySet()) {
            if (!entry.getValue().trim().equals("")) {
                query = query + entry.getKey() + "= '" + entry.getValue() + "' and ";
            }
        }
        if (query.endsWith("and ")) {
            query = query.substring(0, query.length() - 4);
        }
        if (query.endsWith("where ")) {
            query = query.substring(0, query.length() - 6);
        }
        return query;
    }

    public String buildQuery(String tableName, HashMap<String, String> record) {
        String query = "Select * from " + tableName + " where ";
        for (Map.Entry<String, String> entry : record.entrySet()) {
            if (!entry.getValue().trim().equals("")) {
                query = query + entry.getKey() + "= '" + entry.getValue() + "' and ";
            }
        }
        if (query.endsWith("and ")) {
            query = query.substring(0, query.length() - 4);
        }
        if (query.endsWith("where ")) {
            query = query.substring(0, query.length() - 6);
        }
        return query;
    }

    boolean status = false;

    public boolean verifyCheckBoxIsSelectedOrNot(String obj) {
        try {
            WebElement we = getWebElement(obj);


            if (we.isSelected()) {
                testStepInfo("element is checked ");
                status = true;
            } else {

                testStepInfo("element is not checked ");
                status = false;

            }
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
        return status;
    }

    public boolean valueCompare1(String expected, String actual, String searchingFor) {
        boolean status = false;
        try {
            if (actual.trim().equalsIgnoreCase(expected.trim())) {
                testStepInfo("Comparison for " + searchingFor + " successful");
                status = true;
            } else {
                testStepFailed("Value do not match for " + searchingFor);
            }
        } catch (Exception ex) {
            testStepException(new Exception().getStackTrace()[0].getMethodName(), ex);
        }
        return status;
    }

    public void enterText(String obj, int row, String text, String elemName) {
        try {
            obj = obj.replace("row", String.valueOf(row));

            WebElement we = getWebElement(obj);
            sleep(2000);
            we.clear();
            if (!(we.getAttribute("value").length() == 0)) {
                Backspace(we);
            }
            we.sendKeys(Keys.HOME);
            sleep(2000);
            we.sendKeys(text);
            sleep(2000);
            testStepInfo("Entered the value in the  :" + elemName + " field :" + text);
        } catch (Exception ex) {
            testStepFailed("Exception caught while entering the value in :" + elemName + " field, message is->" + ex.getMessage());
        }
    }


    public void jsMoveToElement(WebElement by) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView()", by);
        } catch (Exception e) {
            testStepFailed("Exception occured in scrollToElement() " + e.getMessage());
        }
    }

    public void  launchApplication(){
        ApplicationManager app = new ApplicationManager((System.getProperty("browser", BrowserType.CHROME)));

        WebDriver wd = ApplicationManager.driver;
        app.initUrl();
    }
}



