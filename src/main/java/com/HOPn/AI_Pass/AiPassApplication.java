package com.HOPn.AI_Pass;

import com.HOPn.AI_Pass.model.UserEntity;
import com.HOPn.AI_Pass.model.UserRoleEnum;
import com.HOPn.AI_Pass.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class AiPassApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiPassApplication.class, args);
	}

}
@Component
@AllArgsConstructor
class BootStrap implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	@Override
	public void run(String... args) throws Exception {
		UserEntity userEntity = new UserEntity();
		userEntity.setEmail("abd@gmail.com");
		userEntity.setPassword(passwordEncoder.encode("123456"));
		userEntity.setRole(UserRoleEnum.ADMIN);
		userRepository.save(userEntity);
		UserEntity userEntity1 = new UserEntity();
		userEntity1.setEmail("abd1@gmail.com");
		userEntity1.setPassword(passwordEncoder.encode("123456"));
		userEntity1.setRole(UserRoleEnum.USER);
		userRepository.save(userEntity1);
	}
}
