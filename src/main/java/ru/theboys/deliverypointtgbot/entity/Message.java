package ru.theboys.deliverypointtgbot.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.theboys.deliverypointtgbot.enums.MessageSource;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message extends BaseModel {
    private Date dateTime;

    private int score;


    private Location location;


    private Vendor vendor;


    private Client client;

    private MessageSource messageSource;

    private String text;
}
