package com.ggzed.chat.config;

import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.springframework.boot.elasticsearch.autoconfigure.Rest5ClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ES 媒体类型兼容配置。
 * <p>
 * 服务端是 ES 8.13.4，而 9.x 客户端默认发送
 * {@code Accept: application/vnd.elasticsearch+json; compatible-with=9}，
 * 8.x 服务端不识别 compatible-with=9 会返回 400。
 * 在 HttpClient 底层拦截器中强制改写为 8.x 媒体类型，
 * 使 9.x 客户端可以正常访问 8.x 服务端。
 */
@Configuration
public class ElasticsearchCompatConfig {

    /** 8.x 服务端识别的媒体类型 */
    private static final String ES8_MEDIA_TYPE = "application/vnd.elasticsearch+json; compatible-with=8";

    @Bean
    public Rest5ClientBuilderCustomizer es8MediaTypeCustomizer() {
        return new Rest5ClientBuilderCustomizer() {
            @Override
            public void customize(Rest5ClientBuilder builder) {
                // 无需定制低层 builder
            }

            @Override
            public void customize(HttpAsyncClientBuilder builder) {
                // 最后一个拦截器，覆盖传输层设置的 9.x 媒体类型头
                builder.addRequestInterceptorLast((request, entity, context) -> {
                    request.setHeader("Accept", ES8_MEDIA_TYPE);
                    if (entity != null) {
                        request.setHeader("Content-Type", ES8_MEDIA_TYPE);
                    }
                });
            }
        };
    }
}
