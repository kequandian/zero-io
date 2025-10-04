package com.jfeat.module.io.json.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.module.frontPage.services.domain.dao.QueryFrontPageDao;
import com.jfeat.module.frontPage.services.domain.model.FrontPageRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for FormPageEndpoint#getAllPages
 * Pure Java tests without starting Spring context.
 */
public class FormPageEndpointTest {

    // Create a dynamic proxy for QueryFrontPageDao that returns a predictable list for findFrontPagePage
    private QueryFrontPageDao createStubQueryFrontPageDao(List<FrontPageRecord> stubbed) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("findFrontPagePage".equals(method.getName())) {
                return stubbed;
            }
            return null; // default for other methods not under test
        };
        return (QueryFrontPageDao) Proxy.newProxyInstance(
                QueryFrontPageDao.class.getClassLoader(),
                new Class[]{QueryFrontPageDao.class},
                handler
        );
    }

    private void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    public void testGetAllPagesReturnsRecords() throws Exception {
        // Arrange
        FormPageEndpoint controller = new FormPageEndpoint();

        List<FrontPageRecord> data = new ArrayList<>();
        FrontPageRecord rec = new FrontPageRecord();
        rec.setPageId("66666");
        rec.setTitle("Test Title");
//        rec.setNotes("note");
        data.add(rec);

        QueryFrontPageDao stubDao = createStubQueryFrontPageDao(data);
        inject(controller, "queryFrontPageDao", stubDao);

        Page<FrontPageRecord> page = new Page<>();

        // Act
        Tip tip = controller.getAllPages(
                page,
                1, // pageNum
                10, // pageSize
                null, // tag
                null, // search
                null, // pageId
                null, // title
                null, // notes
                null, // content
                null, // jsonName
                null, // createTime
                null, // updateTime
                null, // orderBy
                null // sort
        );

        // Print results before assertions
        System.out.println("Tip type: " + (tip == null ? "null" : tip.getClass().getName()));
        System.out.println("Page meta: current=" + page.getCurrent() + ", size=" + page.getSize());
        List<FrontPageRecord> records = page.getRecords();
        System.out.println("Records size: " + (records == null ? "null" : records.size()));
        if (records != null) {
            for (int i = 0; i < records.size(); i++) {
                FrontPageRecord r = records.get(i);
                System.out.println("Record[" + i + "]: pageId=" + r.getPageId() + ", title=" + r.getTitle());
            }
        }

        // Assert - page records should be set from stub
        Assertions.assertNotNull(tip, "Tip result should not be null");
        Assertions.assertNotNull(page.getRecords(), "Page records should not be null");
        Assertions.assertEquals(1, page.getRecords().size(), "Should contain one record");
        Assertions.assertEquals("66666", page.getRecords().get(0).getPageId(), "Record pageId should match");
    }

    @Test
    public void testGetAllPagesInvalidSortThrows() {
        FormPageEndpoint controller = new FormPageEndpoint();
        Page<FrontPageRecord> page = new Page<>();

        // Providing orderBy with an invalid sort should throw BusinessException before hitting the DAO
        Assertions.assertThrows(BusinessException.class, () -> controller.getAllPages(
                page,
                1, // pageNum
                10, // pageSize
                null, // tag
                null, // search
                null, // pageId
                null, // title
                null, // notes
                null, // content
                null, // jsonName
                null, // createTime
                null, // updateTime
                "id", // orderBy
                "INVALID" // sort
        ));
    }
}