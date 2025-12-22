package sf.mifi.grechko.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.repositories.UserRepository;

@Service
@Slf4j
public class AdminInitializerService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Value("${app.admin.default-login:admin}")
    private String defaultAdminLogin;

    @Value("${app.admin.default-password:admin123}")
    private String defaultAdminPassword;

    @Value("${app.admin.auto-create:true}")
    private boolean autoCreateAdmin;

    public AdminInitializerService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @PostConstruct
    public void init() {
        if (!autoCreateAdmin) {
            log.info("Автоматическое создание администратора отключено");
            return;
        }

        try {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);

            if (adminCount == 0) {
                log.warn("В системе нет администраторов. Создаю администратора по умолчанию...");

                // Проверяем, существует ли уже пользователь с таким логином
                if (userRepository.existsByLogin(defaultAdminLogin)) {
                    log.error("Пользователь с логином '{}' уже существует", defaultAdminLogin);
                    return;
                }

                User admin = new User();
                admin.setLogin(defaultAdminLogin);
                admin.setPasswdHash(passwordService.hashPassword(defaultAdminPassword));
                admin.setRole(User.Role.ADMIN);

                userRepository.save(admin);

                log.warn("================================================");
                log.warn("СОЗДАН АДМИНИСТРАТОР ПО УМОЛЧАНИЮ");
                log.warn("Логин: {}", defaultAdminLogin);
                log.warn("Пароль: {}", defaultAdminPassword);
                log.warn("ИЗМЕНИТЕ ПАРОЛЬ ПРИ ПЕРВОМ ВХОДЕ!");
                log.warn("================================================");
            } else {
                log.info("В системе уже есть {} администратор(ов)", adminCount);
            }
        } catch (Exception e) {
            log.error("Ошибка при инициализации администратора: {}", e.getMessage());
        }
    }
}
