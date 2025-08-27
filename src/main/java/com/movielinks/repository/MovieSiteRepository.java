package com.movielinks.repository;

import com.movielinks.model.MovieSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieSiteRepository extends JpaRepository<MovieSite, Long> {
    
    /**
     * Find movie site by name
     */
    Optional<MovieSite> findBySiteNameIgnoreCase(String siteName);
    
    /**
     * Find all active movie sites
     */
    List<MovieSite> findByIsActiveTrue();
    
    /**
     * Find sites by status
     */
    List<MovieSite> findByStatus(String status);
    
    /**
     * Find sites that need checking (last checked before given time)
     */
    @Query("SELECT ms FROM MovieSite ms WHERE ms.lastChecked < :checkTime OR ms.lastChecked IS NULL")
    List<MovieSite> findSitesNeedingCheck(@Param("checkTime") LocalDateTime checkTime);
    
    /**
     * Find working sites (status = WORKING and active)
     */
    @Query("SELECT ms FROM MovieSite ms WHERE ms.status = 'WORKING' AND ms.isActive = true")
    List<MovieSite> findWorkingSites();
    
    /**
     * Find sites by domain pattern
     */
    @Query("SELECT ms FROM MovieSite ms WHERE ms.currentWorkingUrl LIKE %:domain%")
    List<MovieSite> findByDomainPattern(@Param("domain") String domain);
    
    /**
     * Count sites by status
     */
    @Query("SELECT ms.status, COUNT(ms) FROM MovieSite ms GROUP BY ms.status")
    List<Object[]> countByStatus();
    
    /**
     * Find recently updated sites
     */
    @Query("SELECT ms FROM MovieSite ms WHERE ms.lastUpdated >= :since ORDER BY ms.lastUpdated DESC")
    List<MovieSite> findRecentlyUpdated(@Param("since") LocalDateTime since);
}