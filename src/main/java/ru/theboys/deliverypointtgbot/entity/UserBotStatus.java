package ru.theboys.deliverypointtgbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.theboys.deliverypointtgbot.enums.BotState;

import java.io.Serializable;

@Entity
@Table(name = "users_bot_status")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserBotStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "chat_id", unique = true, nullable = false, length = 255)
    private String chatId;

    @Enumerated
    @Column(name = "last_bot_state", columnDefinition = "SMALLINT DEFAULT NULL")
    private BotState lastBotState;

}