package tests;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@ContextConfiguration(locations = "/Spring/dataBase.xml")
@ActiveProfiles({"DB1"})
public class TestBase extends AbstractTestNGSpringContextTests {



}
