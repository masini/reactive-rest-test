package net.lucamasini;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void test1() {
        given()
          .when().get("/testcase/1")
          .then()
             .statusCode(500)
//             .body(is("{\"statusCode\":200,\"description\":\"OK\"}"))
        ;
    }

    @Test
    public void test2() {
        final var validatableResponse = given()
                .when().get("/testcase/2")
                .then()
                .statusCode(500);
    }

    @Test
    public void test3() {
        given()
                .when().get("/testcase/3")
                .then()
                .statusCode(500)
        //        .body(is("{\"statusCode\":200,\"description\":\"OK\"}"))
        ;
    }

    @Test
    public void test4() {
        given()
          .when().get("/testcase/4")
          .then()
             .statusCode(200)
                .body(containsString("{\"Content-Type\":\"[application/x-www-form-urlencoded]\"}"))
        ;
    }

    @Test
    public void test5() {
        given()
          .when().get("/testcase/5")
          .then()
             .statusCode(200)
                .body(containsString("{\"Content-Type\":\"[application/x-www-form-urlencoded]\"}"))
        ;
    }

}
