package ru.theboys.deliverypointratingtgbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.theboys.deliverypointratingtgbot.enums.BotState;

import java.io.Serializable;

@Entity
@Table(name = "users_bot_status")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserBotStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "chat_id", unique = true, nullable = false, length = 255)
    private String chatId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "last_bot_state")
    private BotState lastBotState;

    @Column(name = "last_order_id")
    private String lastOrderId;

    @Column(name = "last_score")
    private Integer lastScore;
}