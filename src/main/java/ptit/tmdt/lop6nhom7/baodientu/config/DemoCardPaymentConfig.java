package ptit.tmdt.lop6nhom7.baodientu.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.payments.demo-card")
public class DemoCardPaymentConfig {
  /**
   * Demo-only payment switch. Disable this in every production environment.
   */
  private boolean enabled = false;
}
