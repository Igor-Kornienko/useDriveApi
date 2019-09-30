package kornienko.userauth.controller;

import com.google.api.services.drive.model.File;
import kornienko.userauth.service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    GoogleDriveService googleDriveService;

    @RequestMapping(value = "/urlAuth")
    public RedirectView urlAuth() {
        return googleDriveService.urlAuth();
    }

    //http://localhost:8080/saveAuth?code=4/rQF0wooWSZEjAuN4nn1mUCpDl2Njo243YzusqIsQvumZB68i6Y33fIwzY32rd6u__pA0osBSD9-360aa7AIyduk&scope=https://www.googleapis.com/auth/drive
    @RequestMapping(value = "/codeExchange", method = RequestMethod.GET)
    public RedirectView codeExchange(@RequestParam("code") String accessToken) throws IOException {
        return googleDriveService.codeExchange(accessToken);
    }

    @RequestMapping(value = "/files")
    public List<File> user() throws IOException {
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