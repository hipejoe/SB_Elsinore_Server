package com.sb.elsinore;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.script.ScriptTemplateViewResolver;

@SpringBootApplication
@EnableWebMvc
public class Elsinore implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/", "classpath:/resources/",
            "classpath:/static/", "classpath:/public/"};
    private static Logger logger = LoggerFactory.getLogger(Elsinore.class);

    /**
     * Main method to launch the brewery.
     *
     * @param arguments List of arguments from the command line
     */
    public static void main(final String... arguments) {
        logger.info("Launch", "Launching elsinore with %s", Arrays.toString(arguments));
        new SpringApplicationBuilder(Elsinore.class).run(arguments);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/webjars/**")) {
            registry.addResourceHandler("/webjars/**").addResourceLocations(
                    "classpath:/META-INF/resources/webjars/");
        }
        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations(
                    CLASSPATH_RESOURCE_LOCATIONS);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("POST")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(false)
                .maxAge(32400);  // 9 hours max age
    }

    @Bean
    public ViewResolver reactViewResolver() {
        ScriptTemplateViewResolver viewResolver = new ScriptTemplateViewResolver();
        viewResolver.setPrefix("static/templates/");
        viewResolver.setSuffix(".html");
        return viewResolver;
    }

    // @Bean
    // public ScriptTemplateConfigurer reactConfigurer() {
    //     ScriptTemplateConfigurer configurer = new ScriptTemplateConfigurer();
    //     configurer.setEngineName("nashorn");
    //     configurer.setScripts("/META-INF/resources/webjars/react/16.9.0.react.js");
    //     configurer.setRenderFunction("render");
    //     return configurer;
    // }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/graphiql")
                .setViewName("forward:/graphiql/index.html");
    }
}