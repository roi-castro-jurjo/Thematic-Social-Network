package gal.usc.etse.grei.es;

import gal.usc.etse.grei.es.repository.support.ResourceRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
@EnableMongoRepositories(repositoryBaseClass = ResourceRepositoryImpl.class)
public class Application implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("| READY |");
    }
}
