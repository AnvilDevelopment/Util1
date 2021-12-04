package us.anvildevelopment.v1.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ConfiguratorServer implements Runnable {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void run() {
        if (new File(getFileName(new Config())).exists()) {
            try {
                Config cfg = (Config) getConfig(new Config());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!Config.setup) {
            Integer pin = new Random().nextInt(100000000);
            System.out.println("One or more programs require configuration!");
            //TODO Download link
            System.out.println("Please launch or install Configurator");
            System.out.println("PIN: "+Config.pin);
        }
        System.out.println("Port: "+Config.port);
    }
    public static Configurator getConfig(Configurator c) throws IOException {
        File input = new File(getFileName(c));
        return mapper.readValue(input, c.getClass());
    }
    public static boolean storeConfig(Configurator c) {
        try {
            File output = new File(getFileName(c));
            mapper.writeValue(output, c);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static String getFileName(Configurator c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getProject());
        sb.append("_");
        sb.append(c.getModule());
        return sb.toString();
    }
}
