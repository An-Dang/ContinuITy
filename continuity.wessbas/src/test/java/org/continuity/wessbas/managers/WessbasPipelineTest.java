package org.continuity.wessbas.managers;

import java.util.function.Consumer;

import org.continuity.wessbas.entities.MonitoringData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import m4jdsl.WorkloadModel;

public class WessbasPipelineTest {
	
	// private AmqpTemplate amqpMock;
	
	private RestTemplate restMock;
	
	MonitoringData mData;
	
	WessbasPipelineManager pipelineManager;

	@Before
	public void setup() {
		mData = new MonitoringData();
		mData.setTag("some Tag");
		mData.setLink("some Link");
		String urlString = "http://session-logs?link=" + mData.getLink();
		restMock = Mockito.mock(RestTemplate.class);
		String result = "DAC0E7CAC657D59A1328DEAC1F1F9472;\"ShopGET\":1511777946984000000:1511777947595000000:/dvdstore/browse:8080:localhost:HTTP/1.1:GET:conversationId=1:<no-encoding>;\"HomeGET\":1511777963338000000:1511777963415000000:/dvdstore/home:8080:localhost:HTTP/1.1:GET:<no-query-string>:<no-encoding>;\"ShopGET\":1511779159657000000:1511779159856000000:/dvdstore/browse:8080:localhost:HTTP/1.1:GET:<no-query-string>:<no-encoding>";
		Mockito.when(restMock.getForObject(urlString, String.class)).thenReturn(result);
		
		@SuppressWarnings("unchecked")
		Consumer<WorkloadModel> consumerMock = Mockito.mock(Consumer.class);
		
		pipelineManager = new WessbasPipelineManager(consumerMock, restMock);
		// pipelineManager.runPipeline(data);
	}
	
	@Test
	public void test() {
		pipelineManager.runPipeline(mData);
	}
}
