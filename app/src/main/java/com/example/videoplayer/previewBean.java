package com.example.videoplayer;

public class previewBean {
    private int id;
    private String videoName;
    private String videoTag;
    private String videoDescription;
    private byte[] videoThumb;
    private String uploadDate;
    private int playCount;
    private int like;
    private int dispatchCount;

    public previewBean(int id, String videoName, String videoTag, String videoDescription, byte[] videoThumb, String uploadDate, int playCount, int like, int dispatchCount) {
        this.id = id;
        this.videoName = videoName;
        this.videoTag = videoTag;
        this.videoDescription = videoDescription;
        this.videoThumb = videoThumb;
        this.uploadDate = uploadDate;
        this.playCount = playCount;
        this.like = like;
        this.dispatchCount = dispatchCount;
    }

    public int getId() {
        return id;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getVideoTag() {
        return videoTag;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public byte[] getVideoThumb() {
        return videoThumb;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public int getPlayCount() {
        return playCount;
    }

    public int getLike() {
        return like;
    }

    public int getDispatchCount() {
        return dispatchCount;
    }
}
