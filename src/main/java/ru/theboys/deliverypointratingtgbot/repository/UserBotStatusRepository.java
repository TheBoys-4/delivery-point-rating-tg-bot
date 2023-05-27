package ru.theboys.deliverypointratingtgbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.theboys.deliverypointratingtgbot.entity.UserBotStatus;

@Repository
public interface UserBotStatusRepository extends JpaRepository<UserBotStatus,String> {

    @Query(value = "select * from users_bot_status us where us.chat_id = ?1",nativeQuery = true)
    UserBotStatus getUserBotStatusByChatId(String chatId);
}
