package org.example.demo1.resources;

import org.example.demo1.common.DBUtil;
import org.example.demo1.domain.dao.CustomerDAO;
import org.example.demo1.domain.dao.UserDAO;
import org.example.demo1.domain.daoimpl.CustomerDAOImpl;
import org.example.demo1.domain.daoimpl.UserDAOImpl;
import org.example.demo1.domain.model.Customer;
import org.example.demo1.domain.model.User;
import org.example.demo1.domain.model.SignUpRequest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.util.Base64;

@Path("/signup")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SignupResource {

    @POST
    public Response signup(SignUpRequest signupRequest) {
        String username = signupRequest.getUsername();
        String password = signupRequest.getPassword();
        String role = signupRequest.getRole();
        Customer customer = signupRequest.getCustomerInfo();

        if (username == null || password == null || role == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing required fields\"}")
                    .build();
        }

        if ("customer".equalsIgnoreCase(role)) {
            if (customer == null || customer.getName() == null || customer.getEmail() == null ||
                    customer.getPhone() == null || customer.getAddress() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Customer details (name, email, phone, address) are required\"}")
                        .build();
            }
        }

        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());

        try (Connection conn = DBUtil.getConnection()) {
            UserDAO userDAO = new UserDAOImpl(conn);
            CustomerDAO customerDAO = new CustomerDAOImpl(conn);

            if (userDAO.getUserByUsername(username) != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\":\"Username already exists\"}")
                        .build();
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setRole(role);

            boolean userAdded = userDAO.addUser(user);
            if (!userAdded) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Failed to create user\"}")
                        .build();
            }

            if ("customer".equalsIgnoreCase(role)) {
                User createdUser = userDAO.getUserByUsername(username);
                customer.setUserId(createdUser.getId());

                boolean customerAdded = customerDAO.addCustomer(customer);

                if (!customerAdded) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\":\"User created, but failed to create customer profile\"}")
                            .build();
                }

                return Response.status(Response.Status.CREATED)
                        .entity("{\"message\":\"Customer user created successfully\"}")
                        .build();
            } else {
                return Response.status(Response.Status.CREATED)
                        .entity("{\"message\":\"User created with role: " + role + ", no customer profile created\"}")
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Internal server error\"}")
                    .build();
        }
    }

}
