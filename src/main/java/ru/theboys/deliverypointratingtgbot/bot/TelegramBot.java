package ru.theboys.deliverypointratingtgbot.bot;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theboys.deliverypointratingtgbot.config.BotConfig;
import ru.theboys.deliverypointratingtgbot.constants.CallBackDataConstants;
import ru.theboys.deliverypointratingtgbot.constants.NameConstants;
import ru.theboys.deliverypointratingtgbot.constants.TextConstants;
import ru.theboys.deliverypointratingtgbot.entity.Message;
import ru.theboys.deliverypointratingtgbot.entity.UserBotStatus;
import ru.theboys.deliverypointratingtgbot.enums.BotState;
import ru.theboys.deliverypointratingtgbot.enums.MessageSource;
import ru.theboys.deliverypointratingtgbot.service.TgBotService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {


    final BotConfig config;

    final TgBotService telegramBotService;


    public TelegramBot(BotConfig config, @Autowired TgBotService telegramBotService) {
        this.config = config;
        this.telegramBotService = telegramBotService;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "start"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("error menu list" + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    viewStartMenu(chatId);
                    if (this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId)) == null) {
                        UserBotStatus userBotStatus = new UserBotStatus();
                        userBotStatus.setChatId(String.valueOf(chatId));
                        userBotStatus.setLastBotState(BotState.FREE);
                        this.telegramBotService.addUserBot(userBotStatus);
                    } else {
                        this.telegramBotService.updateStatus(String.valueOf(chatId), BotState.FREE);
                    }
                    break;
            }
            if (this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId)) != null) {
                if (this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId)).getLastBotState() == BotState.COMMENT_WAIT_ORDER_ID) {
                    UserBotStatus userBotStatus = this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId));
                    userBotStatus.setLastOrderId(messageText);
                    userBotStatus.setLastBotState(BotState.COMMENT_WAIT_SCORE);
                    this.telegramBotService.updateUserBotStatus(String.valueOf(chatId), userBotStatus);
                    viewChooseScoreMenu(chatId);

                } else if (this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId)).getLastBotState() == BotState.COMMENT_WAIT_COMMENT) {
                    UserBotStatus userBotStatus = this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId));
                    Message message = new Message(Date.valueOf(LocalDate.now()), userBotStatus.getLastScore(), null,
                            null, null, MessageSource.TELEGRAM_BOT, messageText);
                    //TODO
                    sendMessage(chatId, "Успешно");
                    viewStartMenu(chatId);
                } else if (this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId)).getLastBotState() == BotState.COMMENT_WAIT_COMMENT) {
                    sendMessage(chatId, TextConstants.ERROR_MESSAGE);
                }
            } else {
                sendMessage(chatId, TextConstants.ERROR_MESSAGE);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId)) == null) {
                sendMessage(chatId, TextConstants.ERROR_MESSAGE_NULL_STATE_FROM_DB);
            } else if (callbackData.equals(CallBackDataConstants.ABOUT_ME_BUTTON)) {
                sendMessage(chatId, TextConstants.ABOUT_ME_MESSAGE);
                viewStartMenu(chatId);
            } else if (callbackData.equals(CallBackDataConstants.COMMENT_BUTTON)) {
                if (this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId)) == null) {
                    UserBotStatus userBotStatus = new UserBotStatus();
                    userBotStatus.setChatId(String.valueOf(chatId));
                    userBotStatus.setLastBotState(BotState.COMMENT_WAIT_ORDER_ID);
                    this.telegramBotService.addUserBot(userBotStatus);
                } else {
                    this.telegramBotService.updateStatus(String.valueOf(chatId), BotState.COMMENT_WAIT_ORDER_ID);
                }
                sendMessage(chatId, TextConstants.GIVE_ORDER_ID_MESSAGE);
            } else if (callbackData.equals(CallBackDataConstants.SCORE1)) {
                UserBotStatus userBotStatus = this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId));
                userBotStatus.setLastBotState(BotState.COMMENT_WAIT_COMMENT);
                userBotStatus.setLastScore(1);
                this.telegramBotService.addUserBot(userBotStatus);
                sendMessage(chatId, TextConstants.GIVE_COMMENT_MESSAGE);
            } else if (callbackData.equals(CallBackDataConstants.SCORE2)) {
                UserBotStatus userBotStatus = this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId));
                userBotStatus.setLastBotState(BotState.COMMENT_WAIT_COMMENT);
                userBotStatus.setLastScore(2);
                this.telegramBotService.addUserBot(userBotStatus);
                sendMessage(chatId, TextConstants.GIVE_COMMENT_MESSAGE);
            } else if (callbackData.equals(CallBackDataConstants.SCORE3)) {
                UserBotStatus userBotStatus = this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId));
                userBotStatus.setLastBotState(BotState.COMMENT_WAIT_COMMENT);
                userBotStatus.setLastScore(3);
                this.telegramBotService.addUserBot(userBotStatus);
                sendMessage(chatId, TextConstants.GIVE_COMMENT_MESSAGE);
            } else if (callbackData.equals(CallBackDataConstants.SCORE4)) {
                UserBotStatus userBotStatus = this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId));
                userBotStatus.setLastBotState(BotState.COMMENT_WAIT_COMMENT);
                userBotStatus.setLastScore(4);
                this.telegramBotService.addUserBot(userBotStatus);
                sendMessage(chatId, TextConstants.GIVE_COMMENT_MESSAGE);
            } else if (callbackData.equals(CallBackDataConstants.SCORE5)) {
                UserBotStatus userBotStatus = this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId));
//                userBotStatus.setLastBotState(BotState.COMMENT_WAIT_COMMENT);
                userBotStatus.setLastScore(5);
                this.telegramBotService.addUserBot(userBotStatus);
                sendMessage(chatId, TextConstants.GIVE_COMMENT_MESSAGE);
            }
        }

    }

    private void viewChooseScoreMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(TextConstants.GIVE_SCORE_MESSAGE);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine3 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine4 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine5 = new ArrayList<>();


        var score1Button = new InlineKeyboardButton();
        score1Button.setText(NameConstants.SCORE1);
        score1Button.setCallbackData(CallBackDataConstants.SCORE1);

        var score2Button = new InlineKeyboardButton();
        score2Button.setText(NameConstants.SCORE2);
        score2Button.setCallbackData(CallBackDataConstants.SCORE2);

        var score3Button = new InlineKeyboardButton();
        score3Button.setText(NameConstants.SCORE3);
        score3Button.setCallbackData(CallBackDataConstants.SCORE3);

        var score4Button = new InlineKeyboardButton();
        score4Button.setText(NameConstants.SCORE4);
        score4Button.setCallbackData(CallBackDataConstants.SCORE4);

        var score5Button = new InlineKeyboardButton();
        score5Button.setText(NameConstants.SCORE5);
        score5Button.setCallbackData(CallBackDataConstants.SCORE5);


        rowInLine1.add(score1Button);
        rowInLine2.add(score2Button);
        rowInLine3.add(score3Button);
        rowInLine4.add(score4Button);
        rowInLine5.add(score5Button);

        rowsInLine.add(rowInLine1);
        rowsInLine.add(rowInLine2);
        rowsInLine.add(rowInLine3);
        rowsInLine.add(rowInLine4);
        rowsInLine.add(rowInLine5);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup((markupInLine));

        executeMessage(message);
    }


    private void viewStartMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите необходимое:");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();


        var aboutMeButton = new InlineKeyboardButton();
        aboutMeButton.setText(NameConstants.ABOUT_ME_BUTTON);
        aboutMeButton.setCallbackData(CallBackDataConstants.ABOUT_ME_BUTTON);

        var commentButton = new InlineKeyboardButton();
        commentButton.setText(NameConstants.COMMENT_BUTTON);
        commentButton.setCallbackData(CallBackDataConstants.COMMENT_BUTTON);

        rowInLine1.add(commentButton);
        rowInLine2.add(aboutMeButton);

        rowsInLine.add(rowInLine1);
        rowsInLine.add(rowInLine2);


        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup((markupInLine));

        executeMessage(message);
    }


    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occured: " + e.getMessage());
        }
    }

}
