package org.example.demo1.resources;

import org.example.demo1.common.DBUtil;
import org.example.demo1.common.security.JWTUtil;
import org.example.demo1.domain.dao.ProductDAO;
import org.example.demo1.domain.daoimpl.ProductDAOImpl;
import org.example.demo1.domain.model.Product;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @GET public Response getAllProducts(@HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        try(Connection conn = DBUtil.getConnection()){
            ProductDAO dao = new ProductDAOImpl(conn);
            List<Product> products = dao.getAllProducts();
            return Response.ok(products).build();
        } catch(Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getProductById(@PathParam("id") String id, @HeaderParam("Authorization")  String authHeader){

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        try (Connection conn = DBUtil.getConnection()){
            ProductDAO dao = new ProductDAOImpl(conn);
            Product product = dao.getProductById(Integer.parseInt(id));
            if(product != null){
                return Response.ok(product).build();
            } else{
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch(Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    public Response addProduct(Product product, @HeaderParam("Authorization")  String authHeader) {


        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }


        // Basic validation
        if (product == null || product.getName() == null || product.getName().trim().isEmpty()
                || product.getPrice() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid product: name must not be empty and price must be greater than 0.")
                    .build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            ProductDAO dao = new ProductDAOImpl(conn);
            boolean success = dao.addProduct(product);
            return success ? Response.status(Response.Status.CREATED).build()
                    : Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PUT
    public Response updateProduct(Product product, @HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        if (product == null || product.getName() == null || product.getName().trim().isEmpty()
                || product.getPrice() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid product: name must not be empty and price must be greater than 0.")
                    .build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            ProductDAO dao = new ProductDAOImpl(conn);
            boolean success = dao.updateProduct(product);
            return success ? Response.ok().build()
                    : Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") int id, @HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            ProductDAO dao = new ProductDAOImpl(conn);
            boolean success = dao.deleteProduct(id);
            return success ? Response.ok().build()
                    : Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}