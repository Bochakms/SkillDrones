package com.drones.skilldrones.repository;

import com.drones.skilldrones.model.Region;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByName(String name);

    @Query("SELECT r FROM Region r WHERE within(:point, r.geometry) = true")
    Optional<Region> findRegionByPoint(@Param("point") Point point);
    @Query(value = "SELECT r.* FROM regions r WHERE ST_Within(:point, r.geometry)", nativeQuery = true)
    Region findRegionContainingPoint(@Param("point") Point point);

}
