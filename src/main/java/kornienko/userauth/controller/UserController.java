package kornienko.userauth.controller;

import com.google.api.services.drive.model.File;
import kornienko.userauth.service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    GoogleDriveService googleDriveService;

    @RequestMapping(value = "/files")
    public List<File> user(Principal principal) throws IOException, URISyntaxException {
        return googleDriveService.getFiles();
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = { "multipart/form-data"})
    public @ResponseBody
    String handleFileUpload(@RequestParam("file") MultipartFile newFile,
                            @RequestParam("name") String newName) {
        return googleDriveService.uploadFile(newName, newFile);
    }

    @RequestMapping(value = "/download", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    void handleFileUpload(@RequestParam("id") String newId, HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        googleDriveService.downloadFile(newId, request, response);
    }
}