package com.magic_fans.wizards.config;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Only initialize if no users exist
        if (userRepository.count() == 0) {
            initializeTestUsers();
        }
    }

    private void initializeTestUsers() {
        String[] users = {
                "wizard1|wizard1@test.com|Harry|Potter|White Magic",
                "wizard2|wizard2@test.com|Albus|Dumbledore|White Magic",
                "wizard3|wizard3@test.com|Draco|Malfoy|Black Magic",
                "wizard4|wizard4@test.com|Tom|Riddle|Black Magic",
                "wizard5|wizard5@test.com|Gandalf|Gray|Gray Magic",
                "wizard6|wizard6@test.com|Merlin|Arthur|Gray Magic",
                "wizard7|wizard7@test.com|Elsa|Frozone|Elemental Magic",
                "wizard8|wizard8@test.com|Aang|Avatar|Elemental Magic",
                "wizard9|wizard9@test.com|Jafar|Villain|Black Magic",
                "wizard10|wizard10@test.com|Hermione|Granger|White Magic",
                "wizard11|wizard11@test.com|Ron|Weasley|White Magic",
                "wizard12|wizard12@test.com|Minerva|McGonagall|White Magic",
                "wizard13|wizard13@test.com|Severus|Snape|Gray Magic",
                "wizard14|wizard14@test.com|Filius|Flitwick|Elemental Magic",
                "wizard15|wizard15@test.com|Pomona|Sprout|White Magic",
                "wizard16|wizard16@test.com|Sybill|Trelawney|Gray Magic",
                "wizard17|wizard17@test.com|Remus|Lupin|Elemental Magic",
                "wizard18|wizard18@test.com|Peter|Pettigrew|Black Magic",
                "wizard19|wizard19@test.com|Sirius|Black|Gray Magic",
                "wizard20|wizard20@test.com|James|Potter|White Magic"
        };

        // Encoded password for "password"
        String encodedPassword = "$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy5.d6i";

        for (String userData : users) {
            String[] parts = userData.split("\\|");
            User user = new User();
            user.setUsername(parts[0]);
            user.setEmail(parts[1]);
            user.setFirstName(parts[2]);
            user.setLastName(parts[3]);
            user.setSpecialization(parts[4]);
            user.setPassword(encodedPassword);
            user.setActive(true);

            userRepository.save(user);
        }
    }
}
