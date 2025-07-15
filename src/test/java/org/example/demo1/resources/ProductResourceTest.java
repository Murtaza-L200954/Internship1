package org.example.demo1.resources;

import org.example.demo1.common.security.JWTUtil;
import org.example.demo1.domain.dao.ProductDAO;
import org.example.demo1.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductResourceTest {

    private ProductDAO mockProductDAO;
    private ProductResource productResource;

    private static final String AUTH_HEADER = "Bearer mock.jwt.token";

    @BeforeEach
    void setUp() {
        mockProductDAO = mock(ProductDAO.class);
        //productResource = new ProductResource(mockProductDAO);
    }

    private Product createSampleProduct() {
        Product product = new Product();
        product.setId(1);
        product.setName("Test Product");
        product.setDescription("A product for testing");
        product.setPrice(100.0);
        product.setStock(10);
        return product;
    }

    @Test
    void testGetAllProducts_Success() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("admin");

            List<Product> mockList = Arrays.asList(createSampleProduct());
            when(mockProductDAO.getAllProducts()).thenReturn(mockList);

            Response response = productResource.getAllProducts(AUTH_HEADER);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(mockList, response.getEntity());
        }
    }

    @Test
    void testGetProductById_Found() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("staff");

            Product product = createSampleProduct();
            when(mockProductDAO.getProductById(1)).thenReturn(product);

            Response response = productResource.getProductById("1", AUTH_HEADER);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(product, response.getEntity());
        }
    }

    @Test
    void testGetProductById_NotFound() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("staff");

            when(mockProductDAO.getProductById(1)).thenReturn(null);

            Response response = productResource.getProductById("1", AUTH_HEADER);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testAddProduct_ValidInput() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("admin");

            Product product = createSampleProduct();
            when(mockProductDAO.addProduct(product)).thenReturn(true);

            Response response = productResource.addProduct(product, AUTH_HEADER);
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testAddProduct_InvalidInput() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("admin");

            Product product = new Product(); // invalid: name is null, price is 0
            Response response = productResource.addProduct(product, AUTH_HEADER);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testUpdateProduct_Success() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("staff");

            Product product = createSampleProduct();
            when(mockProductDAO.updateProduct(product)).thenReturn(true);

            Response response = productResource.updateProduct(product, AUTH_HEADER);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testUpdateProduct_NotFound() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("staff");

            Product product = createSampleProduct();
            when(mockProductDAO.updateProduct(product)).thenReturn(false);

            Response response = productResource.updateProduct(product, AUTH_HEADER);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testDeleteProduct_Success() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("admin");

            when(mockProductDAO.deleteProduct(1)).thenReturn(true);

            Response response = productResource.deleteProduct(1, AUTH_HEADER);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testDeleteProduct_NotFound() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("admin");

            when(mockProductDAO.deleteProduct(1)).thenReturn(false);

            Response response = productResource.deleteProduct(1, AUTH_HEADER);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testAccessDenied_InvalidRole() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn("customer");

            Response response = productResource.getAllProducts(AUTH_HEADER);
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testAccessDenied_MissingRole() {
        try (MockedStatic<JWTUtil> jwtMock = Mockito.mockStatic(JWTUtil.class)) {
            jwtMock.when(() -> JWTUtil.extractRoleFromHeader(AUTH_HEADER)).thenReturn(null);

            Response response = productResource.getAllProducts(AUTH_HEADER);
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }
}
