package gal.usc.etse.grei.es.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "IMDB clone REST API",
                description = "API do proxecto de Enxeñaría de Servizos",
                version = "1.0.0",
                contact = @Contact(
                        name = "Roi Castro Jurjo",
                        url = "https://roi-castro-jurjo.github.io/",
                        email = "roi.castro.jurjo@rai.usc.es"
                ),
                license = @License(
                        name = "MIT Licence",
                        url = "https://opensource.org/licenses/MIT")),
        servers = {
                @Server(url = "/", description = "General use server"),
                @Server(url = "testing.tmdbclone.tv", description = "Testing server")
        }
)
@SecurityScheme(
        name = "JWT",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER)
public class OpenAPIConfiguration {
}
