package com.example.webchat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "edit_history")
public class EditHistory {
    private String oldContent;
    private Date editTimestamp;
    private List<String> prevFileIds;
    private List<String> prevFileNames;
}
