package com.jfeat.am.module.ioJson.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = com.jfeat.AmApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class FormPageEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllPagesOverHttp_realDb() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/form/forms")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

        // Print the response body for inspection
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }
}