package ru.theboys.deliverypointtgbot.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.theboys.deliverypointtgbot.entity.UserBotStatus;
import ru.theboys.deliverypointtgbot.enums.BotState;
import ru.theboys.deliverypointtgbot.repository.UserBotStatusRepository;

import java.util.List;

@Service
public class TgBotService {

    private static final String NO_MESSAGE_FOUND = "No message with id %s found";
    @Autowired
    private UserBotStatusRepository userBotStatusRepository;

    @Autowired
    public TgBotService(UserBotStatusRepository userBotStatusRepository) {
        this.userBotStatusRepository = userBotStatusRepository;
    }

    public List<UserBotStatus> getUsersBotStatus() {
        return this.userBotStatusRepository.findAll();
    }

    public UserBotStatus getUserBot(String userBotId) {
        return this.userBotStatusRepository.findById(userBotId).orElseThrow(() -> new EntityNotFoundException(String.format(NO_MESSAGE_FOUND, userBotId)));
    }

    public void addUserBot(UserBotStatus userBot) {
        this.userBotStatusRepository.save(userBot);
    }

    public void deleteUserBot(String userBotId) {
        this.getUserBot(userBotId);
        this.userBotStatusRepository.deleteById(userBotId);
    }

    public UserBotStatus getUserBotStatusByChatId(String chatId) {
        return this.userBotStatusRepository.getUserBotStatusByChatId(chatId);
    }

    public void updateStatus(String chatId, BotState state) {
        UserBotStatus userBotStatusFromDB = this.userBotStatusRepository.getUserBotStatusByChatId(chatId);
        UserBotStatus userBotStatus = new UserBotStatus();
        BeanUtils.copyProperties(userBotStatusFromDB, userBotStatus);
        userBotStatus.setLastBotState(state);
        addUserBot(userBotStatus);
    }

    public void updateUserBotStatus(String chatId, UserBotStatus userBotStatus) {
        UserBotStatus userBotStatusFromDB = this.userBotStatusRepository.getUserBotStatusByChatId(chatId);
        BeanUtils.copyProperties(userBotStatus, userBotStatusFromDB,"id");
        addUserBot(userBotStatus);
    }


}
