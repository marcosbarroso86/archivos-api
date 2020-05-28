package ar.com.opendevsolutions.archivos.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;

//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArchivosTests {

    @LocalServerPort
    private int port;

//    @Before
//    public void beforeTest() throws Exception {
//        RestAssured.baseURI = String.format("http://localhost:%d/archivos", port);
//    }

	
}
