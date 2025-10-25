package ec.com.ecommerce.gateway.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayRouteScannerTest {

    @Mock
    DiscoveryClient discoveryClient;

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    GatewayRouteScanner scanner;

    @Captor
    ArgumentCaptor<String> topicCaptor;

    @Captor
    ArgumentCaptor<String> keyCaptor;

    @Captor
    ArgumentCaptor<String> payloadCaptor;


    @Test
    void shouldPublishEventForEachService() throws Exception {
        List<String> services = Arrays.asList("service-a", "service-b");
        when(discoveryClient.getServices()).thenReturn(services);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        scanner.onApplicationEvent(new org.springframework.context.event.ContextRefreshedEvent(new GenericApplicationContext()));

        verify(kafkaTemplate, times(2)).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        List<String> topics = topicCaptor.getAllValues();
        List<String> keys = keyCaptor.getAllValues();
        List<String> payloads = payloadCaptor.getAllValues();

        assertThat(topics).allMatch(t -> t.equals("gateway-topic"));
        assertThat(keys).containsExactlyInAnyOrderElementsOf(services);
        assertThat(payloads).allMatch(p -> p.equals("{}"));
    }
}

