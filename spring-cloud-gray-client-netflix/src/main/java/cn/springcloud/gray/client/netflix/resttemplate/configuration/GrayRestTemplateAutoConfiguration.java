package cn.springcloud.gray.client.netflix.resttemplate.configuration;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.client.netflix.connectionpoint.RibbonConnectionPoint;
import cn.springcloud.gray.client.netflix.resttemplate.GrayClientHttpRequestIntercptor;
import cn.springcloud.gray.client.netflix.resttemplate.RestTemplateRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@ConditionalOnBean({GrayManager.class, RestTemplate.class})
@ConditionalOnClass(value = {RestTemplate.class, LoadBalanced.class})
public class GrayRestTemplateAutoConfiguration {

    @Autowired
    private GrayRequestProperties grayRequestProperties;
    @Autowired
    private RibbonConnectionPoint ribbonConnectionPoint;


    @Bean
    public GrayClientHttpRequestIntercptor grayClientHttpRequestIntercptor(
            @Autowired(required = false) @LoadBalanced List<RestTemplate> restTemplates) {
        GrayClientHttpRequestIntercptor intercptor = new GrayClientHttpRequestIntercptor(
                grayRequestProperties, ribbonConnectionPoint);
        if (restTemplates != null) {
            restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(intercptor));
        }
        return intercptor;
    }


    @Configuration
    @ConditionalOnProperty(value = "gray.request.track.enabled", matchIfMissing = true)
    public static class GrayTrackRestTemplateConfiguration {

        @Bean
        public RestTemplateRequestInterceptor restTemplateRequestInterceptor() {
            return new RestTemplateRequestInterceptor();
        }

    }
}
