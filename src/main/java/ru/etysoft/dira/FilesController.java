package ru.etysoft.dira;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FilesController {

    private final Path root = Paths.get("uploads");

    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {



        try {

            String name = KeyGenerator.generateId();

            try {

                if(file.getOriginalFilename().endsWith(".vid"))
                {
                    name += ".vid";
                }
                else if(file.getOriginalFilename().endsWith(".img"))
                {
                    name += ".img";
                }
                else
                {
                    name += ".ukwn";
                }

                Files.copy(file.getInputStream(), this.root.resolve(name));
            } catch (Exception e) {
                if (e instanceof FileAlreadyExistsException) {
                    throw new RuntimeException("A file of that name already exists.");
                }

                e.printStackTrace();
                throw new RuntimeException("Unknown copy exception");
            }

            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(name));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage("not uploaded"));
        }
    }

    @GetMapping("/download/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Path file = root.resolve(filename);
        try {
            Resource resource = new UrlResource(file.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

}
