package ru.theboys.deliverypointtgbot.bot;

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
import ru.theboys.deliverypointtgbot.config.BotConfig;
import ru.theboys.deliverypointtgbot.constants.CallBackDataConstants;
import ru.theboys.deliverypointtgbot.constants.NameConstants;
import ru.theboys.deliverypointtgbot.constants.TextConstants;
import ru.theboys.deliverypointtgbot.entity.Message;
import ru.theboys.deliverypointtgbot.entity.UserBotStatus;
import ru.theboys.deliverypointtgbot.entity.Vendor;
import ru.theboys.deliverypointtgbot.enums.BotState;
import ru.theboys.deliverypointtgbot.service.TgBotService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {


    final BotConfig config;

    final TgBotService telegramBotService;
    private Message message;


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
                    message.setVendor(new Vendor(messageText));
                    this.telegramBotService.updateStatus(String.valueOf(chatId), BotState.COMMENT_WAIT_COMMENT);
                    sendMessage(chatId,TextConstants.GIVE_COMMENT_MESSAGE);
                }
                else if (this.telegramBotService.getUserBotStatusByChatId(String.valueOf(chatId)).getLastBotState() == BotState.COMMENT_WAIT_COMMENT) {
                    message.setText(messageText);
                    this.telegramBotService.updateStatus(String.valueOf(chatId), BotState.FREE);
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callbackData.equals(CallBackDataConstants.ABOUT_ME_BUTTON)) {
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
                this.message = new Message();

            }
        }

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
