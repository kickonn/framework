package appmanager;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WebDriverUpdater {

    String CHROME_URL= "https://chromedriver.storage.googleapis.com/";
    String EDGE_URL= "https://msedgedriver.azureedge.net/";
    static String chromePowerShellCmd = "powershell.exe  (Get-Item (Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe').'(Default)').VersionInfo";
    static String edgePowerShellWin10 = "powershell.exe Get-AppxPackage -Name Microsoft.MicrosoftEdge | Foreach Version";
    static String edgePowShellWinServer = "powershell.exe (Get-Item 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe\').VersionInfo";
    static PropertyFileReader reader = new PropertyFileReader("local.properties");
    static String  powerShelCMD =  reader.get("browser.type").equalsIgnoreCase("edge") ? edgePowShellWinServer  : chromePowerShellCmd ;
    static boolean download = false;


    public String checkBrowserVersion( String cmd) {
        Process powerShellProcess = null;


        try {
            powerShellProcess = Runtime.getRuntime().exec(cmd);

            // Getting the results
            powerShellProcess.getOutputStream().close();


            String line;
            try (BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()))) {
                while ((line = stdout.readLine()) != null) {
                    String[] arr = line.split(" ");
                    for (String result : arr) {
                        if (!result.isEmpty() && Character.isDigit(result.charAt(1))) {
                            return result;
                        }

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException  e) {
            e.printStackTrace();

        }
        return null;

    }

    public String matchingVersionInChromeRepo(String version){
        String result = "";
        if(reader.get("browser.type").equalsIgnoreCase("chrome")){
            try {
                System.out.println("====================================== url : "+ CHROME_URL);
                Document doc = Jsoup.connect(CHROME_URL)
                        .timeout(60000)
                        .get();
                Elements allKeys = doc.getElementsContainingOwnText(version.substring(0,8));
                for(Element ele : allKeys){
                    if(ele.ownText().contains("chromedriver_win32.zip")){
                        if(version.substring(0,2).equals(ele.ownText().substring(0,2))){
                            System.out.println(ele.ownText().split("/")[0]);
                            return result = ele.ownText().split("/")[0];
                        }

                    }
                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            return version;
        }
        return null;

    }


    public void downloadBrowserDriver(String version){
        // Lambda if else statement to control browser type download link
        String downloadLink = reader.get("browser.type").equals("chrome") ? CHROME_URL + version + "/chromedriver_win32.zip" : EDGE_URL + version + "/edgedriver_win32.zip";

        try (BufferedInputStream in = new BufferedInputStream(new URL(downloadLink).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(version.replaceAll("\\.","")+".zip")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void unzip(String zipFilePath, String destDirectory)  {
        try {
            File destDir = new File(destDirectory);
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()&&entry.getName().contains(".exe")) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }


    public static void updateBrowserDriver()  {
        WebDriverUpdater updater = new WebDriverUpdater();
        String browserVersion = updater.matchingVersionInChromeRepo(updater.checkBrowserVersion(powerShelCMD));

        String driverName = reader.get("browser.type").equalsIgnoreCase("edge") ? "./drivers/msedgedriver.exe"  : "drivers/chromedriver.exe" ;
        File f = new File(driverName);
        String currentDriverVersionCMD = reader.get("browser.type").equalsIgnoreCase("edge") ? "drivers/MsEdgeDriver -v"  : "drivers/chromeDriver -v" ;
        System.out.println("================================= this is browser version :" + browserVersion.substring(0,2));
        if(f.exists()){
            String driverVersion = updater.matchingVersionInChromeRepo(updater.checkBrowserVersion(currentDriverVersionCMD));
            System.out.println("================================= this is driver version :" + driverVersion.substring(0,2));
            if ( !browserVersion.equals(driverVersion) ){
                updater.downloadBrowserDriver(browserVersion);
                String fileName = browserVersion.replaceAll("\\.","")+".zip";
                updater.unzip("./"+fileName,"./drivers");
            }
        }else{
            System.out.println("We got empty drivers folder, we will download the driver anyway cheers!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            updater.downloadBrowserDriver(browserVersion);
            String fileName = browserVersion.replaceAll("\\.","")+".zip";
            updater.unzip("./"+fileName,"./drivers");
        }




    }

    public static void main(String[] args) throws IOException {



        WebDriverUpdater updater = new WebDriverUpdater();

//        String version = updater.matchingVersionInChromeRepo(updater.checkBrowserVersion(powerShelCMD));
//        System.out.println("================================= this is browser version :" + version.substring(0,8));
//
//        String driverVersion = updater.matchingVersionInChromeRepo(updater.checkBrowserVersion(currentDriverVersionCMD));
//        System.out.println("================================= this is driver version :" + driverVersion.substring(0,8));
//        updater.downloadBrowserDriver(version);
//        String fileName = version.replaceAll("\\.","")+".zip";
//        updater.unzip("./"+fileName,"./drivers");
    }


}
