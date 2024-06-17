package com.example.graaljsapi.service;

import com.example.graaljsapi.model.Script;
import com.example.graaljsapi.repository.ScriptRepository;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ScriptService {

    @Autowired
    private ScriptRepository scriptRepository;

    private final ConcurrentHashMap<Long, Thread> runningScripts = new ConcurrentHashMap<>();

    public Script executeScript(String code, boolean blocking) {
        Script script = new Script();
        script.setCode(code);
        script.setStatus("queued");
        script.setStartTime(LocalDateTime.now());
        scriptRepository.save(script);

        Runnable task = () -> {
            script.setStatus("executing");
            script.setStartTime(LocalDateTime.now());
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            try (Context context = Context.newBuilder("js")
                    .out(stdout)
                    .err(stderr)
                    .build()) {
                Source source = Source.create("js", code);
                context.eval(source);
                script.setStatus("completed");
            } catch (Exception e) {
                script.setStatus("failed");
                script.setStderr(e.getMessage());
            } finally {
                script.setEndTime(LocalDateTime.now());
                script.setStdout(stdout.toString());
                script.setStderr(stderr.toString());
                scriptRepository.save(script);
                runningScripts.remove(script.getId());
            }
        };

        Thread thread = new Thread(task);
        runningScripts.put(script.getId(), thread);

        if (blocking) {
            thread.run();
        } else {
            thread.start();
        }

        return script;
    }

    public List<Script> listScripts(Optional<String> status, Optional<String> sortBy) {
        List<Script> scripts = scriptRepository.findAll();

        if (status.isPresent()) {
            scripts = scripts.stream()
                    .filter(script -> script.getStatus().equals(status.get()))
                    .collect(Collectors.toList());
        }

        if (sortBy.isPresent() && sortBy.get().equals("id")) {
            scripts.sort((s1, s2) -> Long.compare(s2.getId(), s1.getId()));
        } else if (sortBy.isPresent() && sortBy.get().equals("time")) {
            scripts.sort((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()));
        }

        return scripts;
    }

    public Optional<Script> getScript(Long id) {
        return scriptRepository.findById(id);
    }

    public void stopScript(Long id) {
        Thread thread = runningScripts.get(id);
        if (thread != null) {
            thread.interrupt();
            Script script = scriptRepository.findById(id).orElseThrow();
            script.setStatus("stopped");
            script.setEndTime(LocalDateTime.now());
            scriptRepository.save(script);
            runningScripts.remove(id);
        }
    }

    public void removeScript(Long id) {
        scriptRepository.deleteById(id);
    }
}
