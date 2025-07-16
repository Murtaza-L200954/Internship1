package org.example.demo1.resources;

import org.example.demo1.common.DBUtil;
import org.example.demo1.common.security.JWTUtil;
import org.example.demo1.domain.dao.CustomerDAO;
import org.example.demo1.domain.daoimpl.CustomerDAOImpl;
import org.example.demo1.domain.model.Customer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @GET
    public Response getAllCustomers(@HeaderParam("Authorization")  String authHeader) {
        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            CustomerDAO dao = new CustomerDAOImpl(conn);
            return Response.ok(dao.getAllCustomers()).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCustomer(@PathParam("id") int id, @HeaderParam("Authorization")  String authHeader) {
        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin")) ){
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins only").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            CustomerDAO dao = new CustomerDAOImpl(conn);
            boolean success = dao.deleteCustomer(id);
            return success ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
