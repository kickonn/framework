package appmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public  class PropertyFileReader {


    Properties reader ;
    FileInputStream fis;

    public PropertyFileReader(String file) {
        try {
            this.reader = new Properties();
            this.fis = new FileInputStream(System.getProperty("user.dir")+"/src/main/resources/"+file);
            reader.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public  String get(String name){
        return this.reader.getProperty(name);
    }

}




