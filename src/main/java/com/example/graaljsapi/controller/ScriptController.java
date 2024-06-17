package com.example.graaljsapi.controller;

import com.example.graaljsapi.model.Script;
import com.example.graaljsapi.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scripts")
public class ScriptController {

    @Autowired
    private ScriptService scriptService;

    @PostMapping("/execute")
    public Script executeScript(@RequestBody String code, @RequestParam Optional<Boolean> blocking) {
        return scriptService.executeScript(code, blocking.orElse(false));
    }

    @GetMapping
    public List<Script> listScripts(@RequestParam Optional<String> status, @RequestParam Optional<String> sortBy) {
        return scriptService.listScripts(status, sortBy);
    }

    @GetMapping("/{id}")
    public Optional<Script> getScript(@PathVariable Long id) {
        return scriptService.getScript(id);
    }

    @PostMapping("/{id}/stop")
    public void stopScript(@PathVariable Long id) {
        scriptService.stopScript(id);
    }

    @DeleteMapping("/{id}")
    public void removeScript(@PathVariable Long id) {
        scriptService.removeScript(id);
    }
}
