package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(String login, String plainPassword, User.Role role) {
        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Пользователь с логином '" + login + "' уже существует");
        }

        User user = new User();
        user.setLogin(login);
        user.setPasswdHash(passwordService.hashPassword(plainPassword));
        user.setRole(role);

        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Запрещаем удаление последнего администратора
        if (user.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Нельзя удалить последнего администратора");
            }
        }

        userRepository.delete(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public User updateUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Проверяем, что не меняем роль последнего администратора
        if (user.getRole() == User.Role.ADMIN && newRole != User.Role.ADMIN) {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Нельзя изменить роль последнего администратора");
            }
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Проверяем текущий пароль
        if (!passwordService.verifyPassword(currentPassword, user.getPasswdHash())) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }

        user.setPasswdHash(passwordService.hashPassword(newPassword));
        userRepository.save(user);
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByLogin(username)
                    .orElseThrow(() -> new IllegalStateException("Пользователь не найден: " + username));
        }

        throw new IllegalStateException("Пользователь не аутентифицирован");
    }

    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserLogin() {
        return getCurrentUser().getLogin();
    }
}
