package com.paytrack.paytrackpaymentservice.config;

import com.paytrack.paytrackpaymentservice.entity.LimitConfig;
import com.paytrack.paytrackpaymentservice.entity.SystemSetting;
import com.paytrack.paytrackpaymentservice.repository.LimitConfigRepository;
import com.paytrack.paytrackpaymentservice.repository.SystemSettingRepository;
import com.paytrack.shared.enums.ConfigCategory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ConfigSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ConfigSeeder.class);

    private final LimitConfigRepository limitConfigRepository;
    private final SystemSettingRepository systemSettingRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        seedLimitConfigIfEmpty();
        seedSystemSettingsIfEmpty();
    }

    @Transactional
    public void seedLimitConfigIfEmpty() {

        if (limitConfigRepository.count() == 0) {
            LimitConfig config = new LimitConfig();
            limitConfigRepository.save(config);
            log.info("Seeded default limit config");
        }
    }

    @Transactional
    public void seedSystemSettingsIfEmpty() {

        if (systemSettingRepository.count() > 0) {
            return;
        }

        DefaultSetting[] defaults = {
                new DefaultSetting("payment.service.fallback.enabled", "true",
                        "Basculer vers le fallback lorsque le service de paiement est indisponible",
                        ConfigCategory.SYSTEM),
                new DefaultSetting("system.allowed.origins", "http://localhost:3000",
                        "Origines autorisées pour les appels CORS",
                        ConfigCategory.SYSTEM),
                new DefaultSetting("system.maintenance.mode", "false",
                        "Active le mode maintenance (bloque les écritures)",
                        ConfigCategory.SYSTEM),
                new DefaultSetting("fraud.evaluation.enabled", "true",
                        "Activer/desactiver l'évaluation des règles de fraude",
                        ConfigCategory.SYSTEM),
                new DefaultSetting("fraud.high.amount.threshold", "100000",
                        "Seuil au-delà duquel une transaction est considérée à haut risque (MAD)",
                        ConfigCategory.SYSTEM),
                new DefaultSetting("integration.iso20022.enabled", "false",
                        "Connexion vers le cœur bancaire via ISO 20022",
                        ConfigCategory.INTEGRATION),
                new DefaultSetting("integration.ussd.endpoint", "",
                        "Endpoint USSD (campagnes et alertes)",
                        ConfigCategory.INTEGRATION),
                new DefaultSetting("integration.smpp.endpoint", "",
                        "Endpoint SMPP (SMS bancaires)",
                        ConfigCategory.INTEGRATION),
                new DefaultSetting("integration.moov.africa.endpoint", "",
                        "Passerelle de paiement Moov Africa",
                        ConfigCategory.INTEGRATION),
                new DefaultSetting("logging.api.payments.enabled", "true",
                        "Journaliser les appels de l'API de paiement",
                        ConfigCategory.LOGGING),
                new DefaultSetting("logging.api.retention.days", "90",
                        "Durée de conservation des journaux API (jours)",
                        ConfigCategory.LOGGING)
        };

        Arrays.stream(defaults)
                .map(this::toEntity)
                .forEach(systemSettingRepository::save);

        log.info("Seeded {} default system settings", defaults.length);
    }

    private SystemSetting toEntity(DefaultSetting def) {

        SystemSetting setting = new SystemSetting();
        setting.setKey(def.key);
        setting.setValue(def.value);
        setting.setDescription(def.description);
        setting.setCategory(def.category);
        return setting;
    }

    private record DefaultSetting(String key, String value, String description, ConfigCategory category) {
    }
}