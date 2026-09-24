package com.paytrack.paytrackpaymentservice.repository;

import com.paytrack.paytrackpaymentservice.entity.SystemSetting;
import com.paytrack.shared.enums.ConfigCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, UUID> {

    Optional<SystemSetting> findByKey(String key);

    boolean existsByKey(String key);

    List<SystemSetting> findByCategoryOrderByKeyAsc(ConfigCategory category);

    List<SystemSetting> findAllByOrderByCategoryAscKeyAsc();

    void deleteByKey(String key);
}