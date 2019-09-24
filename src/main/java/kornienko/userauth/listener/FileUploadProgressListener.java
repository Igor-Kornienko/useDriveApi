package kornienko.userauth.listener;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;
import java.text.DecimalFormat;

public class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

    private String mFileUploadedName;
    private long bytesCount;

    DecimalFormat df = new DecimalFormat("#.##");

    public FileUploadProgressListener(String fileName, long bytesCount) {
        mFileUploadedName = fileName;
        this.bytesCount = bytesCount;
    }

    @Override
    public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
        if (mediaHttpUploader == null) return;
        switch (mediaHttpUploader.getUploadState()) {
            case INITIATION_STARTED:
                System.out.println("Initiation has started!");
                break;
            case INITIATION_COMPLETE:
                System.out.println("Initiation is complete!");
                break;
            case MEDIA_IN_PROGRESS:
                double percent = mediaHttpUploader.getNumBytesUploaded() / (double) bytesCount;
                System.out.println("Upload: " + mFileUploadedName + " - " + df.format(percent * 100) + "%");
                break;
            case MEDIA_COMPLETE:
                System.out.println("Upload is complete!");
        }
    }
}