package com.seed.databaseseed.parseCsvToRelational;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

//This class is responsible to generate entrepreneur id.
//Some pitches have 2 entrepreneurs or more, and because of that, the id is not the number of the pitch.
@Component
public class EntrepeneurCodeManager {

    private static final Logger logger = Logger.getLogger(EntrepeneurCodeManager.class.getName());
    private static int code = 1;
    private static Map<String, Integer> entrepreneurCode = new HashMap<>();

    public static Integer getEntrepreneurCode(String name) {
        int temp = getOrGenerateCode(name);
        if (temp == 98) logger.info("Entity repeated" + name);
        return temp;
    }

    private static int getOrGenerateCode(String name) {
        if (entrepreneurCode.containsKey(name)) {
            return entrepreneurCode.get(name);
        }
        entrepreneurCode.put(name, code++);
        return entrepreneurCode.get(name);
    }
}