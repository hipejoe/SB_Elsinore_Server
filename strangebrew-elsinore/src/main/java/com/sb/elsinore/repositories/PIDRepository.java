package com.sb.elsinore.repositories;

import com.sb.elsinore.models.PIDModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


/**
 * Created by doug on 25/01/17.
 */
@RepositoryRestResource(collectionResourceRel = "pids", path = "pids")
public interface PIDRepository extends JpaRepository<PIDModel, Long> {
    PIDModel findByTemperatureNameIgnoreCase(@Param("name") String name);
}