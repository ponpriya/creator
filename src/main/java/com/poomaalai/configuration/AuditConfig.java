import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfig {
    @Bean
    public AuditEventRepository auditEventRepository() {
        // Use an in-memory repository for simplicity/development
        return new InMemoryAuditEventRepository();
        // For production, create a custom implementation to store in DB
    }
}
