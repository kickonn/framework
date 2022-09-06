package appmanager;


import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import org.openqa.selenium.remote.BrowserType;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

public class ApplicationManager {
    public static WebDriver driver;
    private static String browser;
    static String target = System.getProperty("target", "local");
    static PropertyFileReader reader = new PropertyFileReader(String.format("local.properties", target));

    public ApplicationManager(String browser) {
        this.browser = browser;
    }

    public ApplicationManager() {
    }

    public static WebDriver getWebDriver() {
        if (driver == null) {
            if (browser.equals(BrowserType.IE)) {
                //System.setProperty("webdriver.ie.driver","./drivers/IEDriverServer.exe");
               // WebDriverManager.iedriver().setup();
                driver = new InternetExplorerDriver();
                driver.manage().deleteAllCookies();
            } else if (browser.equals(BrowserType.CHROME)) {
                //System.setProperty("webdriver.chrome.driver","./drivers/chromedriver.exe");

                WebDriverManager.chromedriver().setup();
                driver=new ChromeDriver();
               // driver = new ChromeDriver();
                driver.manage().deleteAllCookies();
            }
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            return driver;
        }else{
            return driver;
        }

    }
    public  void initUrl()  {
        try {
            getWebDriver().get(reader.get("web.Url"));
            getWebDriver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void stop() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public void initUrl(String userName) {
        try {
            String userid="fhlbny\\" + userName;
            //      String userid = userName;
            getWebDriver().get(reader.get("web.Url"));
            String pwdDecode = new String(Base64.decodeBase64(reader.get("" + userName + "")));
            Robot robot = new Robot();
            robotType(robot, userid);
            robot.keyPress(KeyEvent.VK_TAB);
            robotType(robot, pwdDecode);
            robot.keyPress(KeyEvent.VK_ENTER);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


    public static void robotType(Robot robot, String characters) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(characters);
        clipboard.setContents(stringSelection, null);
        robot.delay(1000);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.delay(1000);
    }


}
