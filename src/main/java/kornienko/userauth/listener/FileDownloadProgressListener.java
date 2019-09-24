package kornienko.userauth.listener;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DecimalFormat;

public class FileDownloadProgressListener implements MediaHttpDownloaderProgressListener {
    private String mFileUploadedName;

    DecimalFormat df = new DecimalFormat("#.##");

    public FileDownloadProgressListener(String mFileUploadedName) throws IOException {
        this.mFileUploadedName = mFileUploadedName;
    }

    @Override
    public void progressChanged(MediaHttpDownloader mediaHttpDownloader) throws IOException {
        if (mediaHttpDownloader == null) return;
        switch (mediaHttpDownloader.getDownloadState()) {
            case NOT_STARTED:
                System.out.println("Initiation has started!");
                break;
            case MEDIA_IN_PROGRESS:
                System.out.println("Download: " + mFileUploadedName + " - " + df.format(mediaHttpDownloader.getProgress() * 100) + "%");
                break;
            case MEDIA_COMPLETE:
                System.out.println("Download is complete!");
        }
    }
}