package com.example.graaljsapi.repository;

import com.example.graaljsapi.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScriptRepository extends JpaRepository<Script, Long> {
}