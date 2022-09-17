package tests;

import appmanager.*;
import com.google.common.collect.ImmutableMap;
import cucumber.api.CucumberOptions;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//import static com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter;



@CucumberOptions(features ={"classpath:features/GreenKart.feature",
       // "classpath:features/GreenKart.feature",
                              },
        glue = "tests",
        plugin ={"appmanager.ExtentCucumberFormatter"},
//       plugin = {"pretty"},
 // dryRun = true,
        //tags={"@Smoke11,@Regression11,@Functional11"})
        tags={"@GreenKart1"})


public class TestRunner extends AbstractTestNGCucumberTests {
    String sourceDir = "./src/main/resources/";

    public TestRunner() {
    }

    private static ExtentCucumberFormatter formatter;
    private PropertyFileReader localreader = new PropertyFileReader("local.properties");
    EmailSender email = new EmailSender();
    static ArrayList<String> listOfScenarios = new ArrayList<>();
    static ArrayList<String> results = new ArrayList<>();
    String emailSenderSwitch = localreader.get("send.email").replaceAll("\\s+", "");
    String[] emailAddresses = localreader.get("send.emailAddress").replaceAll("\\s+", "").split(",");
    private static final Regex myRegex = new Regex("[^\\\\p{Alpha}\\\\p{Digit}]+");


    @BeforeSuite
    public void setUp() {
      //  WebDriverUpdater.updateBrowserDriver();     // browser driver update
        formatter = new ExtentCucumberFormatter();
        ExtentCucumberFormatter.initiateExtentCucumberFormatter();
        ExtentCucumberFormatter.loadConfig(new File(sourceDir + "extent-config.xml"));
//        allureEnvironmentWriter(
//                ImmutableMap.<String, String>builder()
//                        .put("Browser", "Chrome")
//                        .put("Stand", "Production")
//                        .put("URL", "https://prod-ci:8443/home")
//                        .put("Database.Server", "agl-entdbp2")
//                        .put("Application.Server", "lx-bsapp1")
//                        .build(), System.getProperty("user.dir")
//                        + "/build/allure-results/");

        if (emailSenderSwitch.equalsIgnoreCase("On")) {
            for (String emailAddress : emailAddresses) {
                String message = email.buildMessage();
                email.sendHTMLmessage(emailAddress, "Started to run " + localreader.get("application.name") + " automation scripts.", message);
            }
        }

    }

    @AfterSuite(alwaysRun = true)
    public void tearDown() {
        formatter.releaseMediaPlayer();
        ApplicationManager.stop();
        if (emailSenderSwitch.equalsIgnoreCase("On")) {
            Comparator<String> comparator = Comparator.<String, Boolean>comparing(s -> s.contains("FAILED")).reversed().thenComparing(Comparator.naturalOrder());
            for (String emailAddress : emailAddresses) {
                Collections.sort(listOfScenarios, comparator);
                String message = email.buildPostMessage(listOfScenarios);
                email.sendHTMLmessage(emailAddress, "Execution of " + localreader.get("application.name") + " Selenium scripts completed. ", message);
            }
        }
    }


    @Before
    public void startScenario(Scenario scenario) {

        String fileName = scenario.getName().split(" ")[0];
        String[] tagsToBeRun = localreader.get("tagsForVideoCapture").replaceAll("\\s+", "").split(",");
        for (String tag : scenario.getSourceTagNames()) {
            for (String tagToBeRun : tagsToBeRun) {
                if (tag.equalsIgnoreCase(tagToBeRun)) {
                    System.out.println("================================= " + tag + " ==========================================");
                    HelperBase.screenShotSwitch = formatter.startVideoRecording(fileName);
                } else {
                    HelperBase.screenShotSwitch = false;
                }
            }
        }

    }


    @After
    public void endScenario(Scenario scenario) {
      // ApplicationManager.stop();
        listOfScenarios.add(scenario.getStatus().toUpperCase() + " - " + scenario.getName());
        formatter.stopVideoRecording();
    }

}
