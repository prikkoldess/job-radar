package com.jobscan;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.jobscan.bot.handler.CommandHandler;
import com.jobscan.bot.state.BotState;
import com.jobscan.bot.state.BotStateService;
import com.jobscan.bot.state.BotUserService;
import com.jobscan.vacancy.service.VacancyService;

@ExtendWith(MockitoExtension.class)
class CommandHandlerTest {

    @Mock
    private BotStateService botStateService;

    @Mock
    private BotUserService botUserService;

    @Mock
    private VacancyService vacancyService;

    @Mock
    private TelegramClient telegramClient;

    @InjectMocks
    private CommandHandler commandHandler;

    @Test
    void shouldHandleStartCommandAndSendWelcomeMessage() throws Exception {

        Long chatId = 12345L;
        Message mockMessage = mock(Message.class);
        when(mockMessage.getText()).thenReturn("/start");
        when(mockMessage.getChatId()).thenReturn(chatId);
        when(botStateService.getState(chatId)).thenReturn(BotState.DEFAULT);

        commandHandler.handle(mockMessage, telegramClient);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo(chatId.toString());
        assertThat(sentMessage.getText()).contains("Hi! I am <b>Auto-Job Scan</b>");
    }

    @Test
    void shouldHandleUrlCommandAndChangeBotState() throws Exception {

        Long chatId = 12345L;
        Message mockMessage = mock(Message.class);
        when(mockMessage.getText()).thenReturn("/url");
        when(mockMessage.getChatId()).thenReturn(chatId);
        when(botStateService.getState(chatId)).thenReturn(BotState.DEFAULT);

        commandHandler.handle(mockMessage, telegramClient);

        verify(botStateService).setState(chatId, BotState.WAITING_FOR_URL);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).contains("Submit a new link");
    }
}
