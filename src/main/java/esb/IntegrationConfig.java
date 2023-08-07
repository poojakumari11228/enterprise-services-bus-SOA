package esb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Bean
    public MessageChannel wharehousechannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel shippingchannel() {
        return new DirectChannel();
    }

    @Bean
    public WarehouseActivator warehouseservice() {
        return new WarehouseActivator();
    }

    @Bean
    public NormalShippingActivator shippingservice() {
        return new NormalShippingActivator();
    }

    @Bean
    public NextDayShippingActivator nextdayshippingservice() {
        return new NextDayShippingActivator();
    }

    @Bean
    public InternationalShippingActivator internationalShippingService() {
        return new InternationalShippingActivator();
    }

    @Bean
    public IntegrationFlow integrationFlow() {
        return IntegrationFlows.from("wharehousechannel")
                .handle(warehouseservice(), "checkStock")
                .channel("shippingchannel")
                .route("payload.orderType",
                        mapping -> mapping
                                .subFlowMapping("international", sf -> sf.handle(internationalShippingService(), "ship"))
                                .subFlowMapping("domestic", sf -> sf.route("payload.amount < 175",
                                        mapping2 -> mapping2
                                                .subFlowMapping(true, sf2 -> sf2.handle(shippingservice(), "ship"))
                                                .subFlowMapping(false, sf2 -> sf2.handle(nextdayshippingservice(), "ship"))
                                )))
                                .get();
    }

}
