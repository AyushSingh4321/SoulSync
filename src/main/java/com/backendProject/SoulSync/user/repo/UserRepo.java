package com.backendProject.SoulSync.user.repo;

import com.backendProject.SoulSync.user.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserModel, Integer> {

    Optional<UserModel> findByEmail(String email);

    Optional<UserModel> findByUsername(String username);

    @Query("SELECT u FROM UserModel u WHERE u.email = :email")
    UserModel getUserDetailsByEmail(@Param("email") String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserModel u WHERE u.email <> :email")
    List<UserModel> findAllByEmailExcept(@Param("email") String email);

    @Query("SELECT u FROM UserModel u WHERE u.id <> :currentUserId")
    List<UserModel> findAllExceptCurrent(@Param("currentUserId") Integer currentUserId);


}
