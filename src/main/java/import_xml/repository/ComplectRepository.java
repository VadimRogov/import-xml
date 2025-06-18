package import_xml.repository;

import import_xml.model.Complect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplectRepository extends JpaRepository<Complect, Long> {
    Complect findByComplectId(String complectId);
    void deleteByComplectId(String complectId);
}
