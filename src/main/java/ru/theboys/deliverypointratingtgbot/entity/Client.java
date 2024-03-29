package ru.theboys.deliverypointratingtgbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    private String name;

    private String phoneNumber;

    private String email;

    private String sex;

    private int age;
}
