package org.example.demo1.resources;

import org.example.demo1.common.DBUtil;
import org.example.demo1.common.security.JWTUtil;
import org.example.demo1.domain.dao.CustomerDAO;
import org.example.demo1.domain.dao.UserDAO;
import org.example.demo1.domain.daoimpl.CustomerDAOImpl;
import org.example.demo1.domain.daoimpl.UserDAOImpl;
import org.example.demo1.domain.model.Customer;
import org.example.demo1.domain.model.User;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList("admin", "customer", "staff"));

    @POST
    @Deprecated
    public Response createUser(User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        String role = user.getRole();

        if (username == null || password == null || role == null ||
                username.isEmpty() || password.isEmpty() || role.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Username, password, and role are required.")
                    .build();
        }

        if (!VALID_ROLES.contains(role.toLowerCase())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid role. Allowed roles: admin, customer, staff.")
                    .build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            UserDAO userDAO = new UserDAOImpl(conn);

            if (userDAO.userExists(username)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Username already exists.")
                        .build();
            }

            String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
            user.setPassword(encodedPassword);
            user.setRole(role.toLowerCase());

            boolean success = userDAO.addUser(user);
            if (success) {
                return Response.status(Response.Status.CREATED)
                        .entity("User created.")
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Failed to create user.")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal server error.")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") int id) {
        try (Connection conn = DBUtil.getConnection()) {
            UserDAO userDAO = new UserDAOImpl(conn);
            User user = userDAO.getUserById(id);
            if (user != null) {
                user.setPassword(null); // hide password
                return Response.ok(user).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User not found.")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") int id, User updatedUser,@HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        if (updatedUser.getUsername() == null || updatedUser.getPassword() == null || updatedUser.getRole() == null ||
                updatedUser.getUsername().isEmpty() || updatedUser.getPassword().isEmpty() || updatedUser.getRole().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Username, password, and role are required.")
                    .build();
        }

        if (!VALID_ROLES.contains(updatedUser.getRole().toLowerCase())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid role. Allowed roles: admin, customer, staff.")
                    .build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            UserDAO userDAO = new UserDAOImpl(conn);
            CustomerDAO customerDAO = new CustomerDAOImpl(conn);

            User existingUser = userDAO.getUserById(id);
            if (existingUser == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User not found.").build();
            }

            updatedUser.setId(id);
            updatedUser.setPassword(Base64.getEncoder().encodeToString(updatedUser.getPassword().getBytes()));
            updatedUser.setRole(updatedUser.getRole().toLowerCase());

            boolean updated = userDAO.updateUser(updatedUser);
            if (!updated) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to update user.").build();
            }

            if ("customer".equalsIgnoreCase(updatedUser.getRole())) {
                Customer existingCustomer = customerDAO.getCustomerByUserId(id);
                if (existingCustomer != null) {
                    existingCustomer.setName(updatedUser.getUsername()); // or pass more info if you have it
                    boolean customerUpdated = customerDAO.updateCustomer(existingCustomer);
                    if (!customerUpdated) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("User updated, but failed to update customer info.").build();
                    }
                }
            }

            return Response.ok("User updated successfully.").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Server error.").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") int id, @HeaderParam("Authorization")  String authHeader) {

        String role = JWTUtil.extractRoleFromHeader(authHeader);

        if (role == null || !(role.equals("admin") || role.equals("staff"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied: Admins and staff only").build();
        }

        try (Connection conn = DBUtil.getConnection()) {
            UserDAO userDAO = new UserDAOImpl(conn);
            CustomerDAO customerDAO = new CustomerDAOImpl(conn);

            User existingUser = userDAO.getUserById(id);
            if (existingUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found.").build();
            }

            if ("customer".equalsIgnoreCase(existingUser.getRole())) {
                customerDAO.deleteCustomerByUserId(id); // Optional: check success
            }

            boolean deleted = userDAO.deleteUser(id);
            return deleted
                    ? Response.ok("User deleted successfully.").build()
                    : Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to delete user.").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Server error.").build();
        }
    }
}
