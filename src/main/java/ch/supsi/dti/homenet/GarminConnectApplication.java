package ch.supsi.dti.homenet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class GarminConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(GarminConnectApplication.class, args);
    }

}
