package com.example;

import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class DataLoader implements ApplicationEventListener<StartupEvent> {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DataLoader(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(StartupEvent event) {
        // Create roles if they don't exist
        createRoleIfNotExists(Role.RoleName.ADMIN, "Administrator role");
        createRoleIfNotExists(Role.RoleName.USER, "Regular user role");
        createRoleIfNotExists(Role.RoleName.MODERATOR, "Moderator role");

        // Create default admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            admin.setFirstName("Admin");
            admin.setLastName("User");

            Set<Role> adminRoles = new HashSet<>();
            roleRepository.findByName(Role.RoleName.ADMIN).ifPresent(adminRoles::add);
            admin.setRoles(adminRoles);

            userRepository.save(admin);
        }
    }

    private void createRoleIfNotExists(Role.RoleName roleName, String description) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role(roleName, description);
            roleRepository.save(role);
        }
    }
}