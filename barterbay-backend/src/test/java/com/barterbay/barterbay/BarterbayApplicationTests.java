package com.barterbay.barterbay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.barterbay.barterbay.repository.ExchangeRequestRepository;
import com.barterbay.barterbay.repository.ReportRepository;
import com.barterbay.barterbay.repository.UserRepository;

@SpringBootTest(properties = {
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
		+ "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
		+ "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"
})
class BarterbayApplicationTests {

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private ReportRepository reportRepository;

	@MockBean
	private ExchangeRequestRepository exchangeRequestRepository;

	@Test
	void contextLoads() {
	}

}
