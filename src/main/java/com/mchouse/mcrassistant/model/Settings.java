package com.mchouse.mcrassistant.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Settings implements Serializable {
    private String sourceEmail;
    private String altSourceEmail;
    private String sourceEmailToken;
    private String mailBody;
    private String mailSubject;
    private String mailSignature;
    private String lastExcelFileFolder;
}
