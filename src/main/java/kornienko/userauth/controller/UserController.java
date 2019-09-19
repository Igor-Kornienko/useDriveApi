package kornienko.userauth.controller;

import com.google.api.services.drive.model.File;
import kornienko.userauth.service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    GoogleDriveService googleDriveService;

    @RequestMapping(value = "/files")
    public List<File> user(Principal principal) throws IOException, GeneralSecurityException, URISyntaxException {
        System.out.println("/files");
        return googleDriveService.getFiles();
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = { "multipart/form-data"})
    public @ResponseBody
    String handleFileUpload(@RequestParam("file") MultipartFile newFile,
                            @RequestParam("name") String newName) {
        System.out.println("/upload");
        return googleDriveService.uploadFile(newName, newFile);
    }

    @RequestMapping(value = "/download", method = RequestMethod.POST)
    public @ResponseBody
    byte[] handleFileUpload(@RequestParam("id") String newId) throws IOException {
        System.out.println("/download");
        return googleDriveService.downloadFile(newId);
    }
}