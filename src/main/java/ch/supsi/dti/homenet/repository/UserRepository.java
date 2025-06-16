package ch.supsi.dti.homenet.repository;

import ch.supsi.dti.homenet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByUuid(String uuid);
    Optional<User> findOneByToken(String token);
    List<User> findByDiseaseCategoryId(Long categoryId);
}
