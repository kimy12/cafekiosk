package sample.cafekiosk.spring.api.service.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sample.cafekiosk.spring.client.mail.MailSendClient;
import sample.cafekiosk.spring.domain.mail.MailSendHistory;
import sample.cafekiosk.spring.domain.mail.MailSendHistoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private MailSendClient mailSendClient;

    @Mock
    private MailSendHistoryRepository mailSendHistoryRepository;

    @InjectMocks
    private MailService mailService;

    @DisplayName("메일 전송 테스트")
    @Test
    void sendMail(){
        //given
//        MailSendClient mailSendClient = mock(MailSendClient.class);
//        MailSendHistoryRepository mailSendHistoryRepository = mock(MailSendHistoryRepository.class);
//        MailService mailService = new MailService(mailSendClient, mailSendHistoryRepository);

        // stubbing
//        when(mailSendClient.sendEmail(anyString(),anyString(),anyString(),anyString()))
//                .thenReturn(true);

        // 현재 given, when, then BDD스타일로 테스트를 하다보니 동작은 동일하지만 이름만 다른 BDDMockito를 사용하는게 좋음
        BDDMockito.given(mailSendClient.sendEmail(anyString(),anyString(),anyString(),anyString()))
                .willReturn(true);



        //when
        boolean result = mailService.sendMail("", "", "", "");

        //then
        assertThat(result).isTrue();
        verify(mailSendHistoryRepository, Mockito.times(1)).save(any(MailSendHistory.class));
    }
}