package ar.com.opendevsolutions.archivos.rest;

public class UploadFileResponse {
    public String fileName;
    public String fileDownloadUri;
    public String fileType;
    public long size;

    public UploadFileResponse(String fileName, String fileDownloadUri, String fileType, long size) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
    }

}
