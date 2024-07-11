package com.example.webchat.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "edit_history")
public class EditHistory {
    private String oldContent;
    private Date editTimestamp;
    private List<String> prevFileIds;
    private List<String> prevFileNames;

    public EditHistory(String oldContent, Date editTimestamp, List<String> prevFileIds, List<String> prevFileNames) {
        this.oldContent = oldContent;
        this.editTimestamp = editTimestamp;
        this.prevFileIds = prevFileIds;
        this.prevFileNames = prevFileNames;
    }

    public String getOldContent() {
        return oldContent;
    }

    public void setOldContent(String oldContent) {
        this.oldContent = oldContent;
    }

    public Date getEditTimestamp() {
        return editTimestamp;
    }

    public void setEditTimestamp(Date editTimestamp) {
        this.editTimestamp = editTimestamp;
    }

    public List<String> getPrevFileIds() {
        return prevFileIds;
    }

    public void setPrevFileIds(List<String> prevFileIds) {
        this.prevFileIds = prevFileIds;
    }

    public List<String> getPrevFileNames() {
        return prevFileNames;
    }

    public void setPrevFileNames(List<String> prevFileNames) {
        this.prevFileNames = prevFileNames;
    }
}
