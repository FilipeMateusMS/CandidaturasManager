package com.candidaturasmanager.repository;

import com.candidaturasmanager.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform, Long>
{
    List<Platform> findByStAtivoTrueOrderByDtUltimoAcessoAsc();
    List<Platform> findByStAtivoFalseOrderByNmPlatformAsc();
}
