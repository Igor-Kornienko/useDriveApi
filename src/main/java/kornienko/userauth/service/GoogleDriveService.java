package kornienko.userauth.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import kornienko.userauth.listener.FileDownloadProgressListener;
import kornienko.userauth.listener.FileUploadProgressListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private final String APPLICATION_NAME = "userauth";

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final NetHttpTransport HTTP_TRANSPORT;

    private final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private final String CREDENTIALS_FILE_PATH = "credentials.json";

    public GoogleDriveService() throws GeneralSecurityException, IOException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    }

    private Drive getDrive() throws IOException {
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials() throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new FileInputStream(new java.io.File(CREDENTIALS_FILE_PATH))));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    private File _createGoogleFile(String googleFolderIdParent, String contentType,
                                          String customFileName, AbstractInputStreamContent uploadStreamContent, long size) throws IOException {

        File fileMetadata = new File();
        fileMetadata.setName(customFileName);

        List<String> parents = Collections.singletonList(googleFolderIdParent);
        fileMetadata.setParents(parents);

        Drive driveService = getDrive();

        Drive.Files.Create createF = driveService.files().create(fileMetadata, uploadStreamContent)
                .setFields("id, webContentLink, webViewLink, parents");

        MediaHttpUploader uploader = createF.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(false);
        uploader.setChunkSize(1024 * 1024 * 8);
        uploader.setProgressListener(new FileUploadProgressListener(fileMetadata.getName(), size));

        return createF.execute();
    }

    public File createGoogleFile(String googleFolderIdParent, String contentType,
                                        String customFileName, InputStream inputStream, long size) throws IOException {

        AbstractInputStreamContent uploadStreamContent = new InputStreamContent(contentType, inputStream);
        return _createGoogleFile(googleFolderIdParent, contentType, customFileName, uploadStreamContent, size);
    }

    public List<File> getFiles() throws IOException, URISyntaxException {
        System.out.println(SCOPES.toString());
        Drive service = getDrive();

        FileList result = service.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        return files;
    }

    public String uploadFile(String name, MultipartFile file){
        if (!file.isEmpty()) {
            try {
                if (name.equals("")) {
                    name = file.getOriginalFilename();
                }
                return "File ID: " + createGoogleFile(null, file.getContentType(), name, file.getInputStream(), file.getSize()).getId();
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return "file is empty!";
        }
    }

    public void downloadFile(String id, HttpServletRequest request,
                                              HttpServletResponse response) throws IOException {

        Drive service = getDrive();
        Drive.Files.Get fileGet = service.files().get(id);
        File gFile = fileGet.execute();

        response.setHeader("Content-Disposition", "attachment; filename=" + gFile.getName());
        response.setHeader("Content-Type", gFile.getMimeType());

        MediaHttpDownloader downloader = fileGet.getMediaHttpDownloader();
        downloader.setDirectDownloadEnabled(false);
        downloader.setChunkSize(1024 * 1024 * 8);
        downloader.setProgressListener(new FileDownloadProgressListener(gFile.getName()));
        fileGet.executeMediaAndDownloadTo(response.getOutputStream());
    }
}
