package kornienko.userauth.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private final String APPLICATION_NAME = "userauth";

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final String TOKENS_DIRECTORY_PATH = "src/main/resources/tokens";

    private final NetHttpTransport HTTP_TRANSPORT;

    private final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private final String CREDENTIALS_FILE_PATH = "credentials.json";

    private static CredentialStore credentialStore;

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

        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
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
                java.io.File newFile = new java.io.File(name);

                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(newFile));
                stream.write(bytes);
                stream.close();

                Drive service = getDrive();

                File fileMetadata = new File();
                fileMetadata.setName(name);

                Tika tika = new Tika();
                String mimeType = tika.detect(newFile);

                System.out.println("mimetype: " + mimeType);

                FileContent mediaContent = new FileContent(mimeType, newFile);
                File gFile = service.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                newFile.delete();
                return "File ID: " + gFile.getId();
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return "file is empty!";
        }
    }

    public byte[] downloadFile(String id) throws IOException {
        Drive service = getDrive();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        File file = service.files().get(id).execute();
        service.files().export(id, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .executeAndDownloadTo(outputStream);

        java.io.File newfile = new java.io.File(file.getName() + '.' + file.getFileExtension());
        newfile.createNewFile();

        FileOutputStream fop = new FileOutputStream(newfile);
        fop.write(outputStream.toByteArray());
        fop.flush();
        fop.close();

        return outputStream.toByteArray();
    }
}
