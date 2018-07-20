package cc.dille.restdocs.openapi;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.Map;

import org.junit.Test;
import org.springframework.restdocs.operation.Operation;

public class JwtScopeHandlerTest {

    private JwtScopeHandler jwtScopeHandler = new JwtScopeHandler();

    private Map<String, Object> model;

    private Operation operation;

    @Test
    public void should_add_scope_list() {
        givenRequestWithJwtInAuthorizationHeader();

        whenModelGenerated(operation);

        then(model).containsKeys("scopes");
        then(model.get("scopes")).isInstanceOf(String.class);
        then(model.get("scopes")).isEqualTo("[\"scope1\", \"scope2\"]");
    }

    @Test
    public void should_do_nothing_when_authorization_header_missing() {
        givenRequestWithoutAuthorizationHeader();

        whenModelGenerated(operation);

        then(model).isEmpty();
    }

    @Test
    public void should_do_nothing_when_token_not_jwt() {
        givenRequestWithNonJwtInAuthorizationHeader();

        whenModelGenerated(operation);

        then(model).isEmpty();
    }

    @Test
    public void should_do_nothing_when_authorization_header_does_not_contain_jwt() {
        givenRequestWithBasicAuthHeader();

        whenModelGenerated(operation);

        then(model).isEmpty();
    }

    private void whenModelGenerated(Operation operation) {
        model = jwtScopeHandler.generateModel(operation, OpenAPIResourceSnippetParameters.builder().build());
    }

    private void givenRequestWithJwtInAuthorizationHeader() {
        operation = new OperationBuilder().request("/some")
                    .header(AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiZXhwIjoxNTA3NzU4NDk4LCJpYXQiOjE1MDc3MTUyOTgsImp0aSI6IjQyYTBhOTFhLWQ2ZWQtNDBjYy1iMTA2LWU5MGNkYWU0M2Q2ZCJ9.eWGo7Y124_Hdrr-bKX08d_oCfdgtlGXo9csz-hvRhRORJi_ZK7PIwM0ChqoLa4AhR-dJ86npid75GB9IxCW2f5E24FyZW2p5swpOpfkEAA4oFuj7jxHiaiqL_HFKKCRsVNAN3hGiSp9Hn3fde0-LlABqMaihdzZzHL-xm8-CqbXT-qBfuscDImZrZQZqhizpSEV4idbEMzZykggLASGoOIL0t0ycfe3yeuQkMUhzZmXuu08VM7zXwWnqfXCa-RmA6wC7ZnWqiJoi0vBr4BrlLR067YoUrT6pgRfiy2HZ0vEE_XY5SBtA-qI2QnlJb7eTk7pgFtoGkYdeOZ86k6GDVw")
                    .build();
    }

    private void givenRequestWithNonJwtInAuthorizationHeader() {
        operation = new OperationBuilder().request("/some")
                .header(AUTHORIZATION, "Bearer ey")
                .build();
    }

    private void givenRequestWithoutAuthorizationHeader() {
        operation = new OperationBuilder().request("/some")
                .build();
    }

    private void givenRequestWithBasicAuthHeader() {
        operation = new OperationBuilder().request("/some")
                .header(AUTHORIZATION, "Basic dGVzdDpwd2QK")
                .build();
    }
}