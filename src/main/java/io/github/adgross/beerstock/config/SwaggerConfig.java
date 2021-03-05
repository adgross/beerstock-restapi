package io.github.adgross.beerstock.config;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Setter
@Configuration
@ConfigurationProperties("api.swagger")
public class SwaggerConfig {

  private String basePackage;
  private String apiTitle;
  private String apiDescription;
  private String apiVersion;
  private String contactName;
  private String contactGithub;
  private String contactEmail;
  private String license;
  private String licenseUrl;

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(basePackage(basePackage))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(buildApiInfo());
  }

  private ApiInfo buildApiInfo() {
    return new ApiInfoBuilder()
        .title(apiTitle)
        .description(apiDescription)
        .version(apiVersion)
        .contact(new Contact(contactName, contactGithub, contactEmail))
        .license(license)
        .licenseUrl(licenseUrl)
        .build();
  }
}
