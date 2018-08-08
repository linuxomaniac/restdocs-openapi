package cc.dille.restdocs.openapi.example.doc;

import cc.dille.restdocs.openapi.example.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@ContextConfiguration(classes = Application.class)
@WebAppConfiguration
@SpringBootTest
public abstract class RestDocTest extends AbstractJUnit4SpringContextTests {
    @Autowired
    private WebApplicationContext context;

    static MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(
            "build/generated-snippets");

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                        .uris()
                        .withScheme("http")
                        .withHost("localhost")
                        .withPort(8080))
                .build();
    }
}
