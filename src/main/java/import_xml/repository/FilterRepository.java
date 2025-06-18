package import_xml.repository;

import import_xml.model.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends JpaRepository<Filter, Long> {
    Filter findByFilterId(String filterId);
    void deleteByFilterId(String filterId);
}
