package org.example.demo1.resources;

import org.example.demo1.common.DBUtil;
import org.example.demo1.domain.dao.UserDAO;
import org.example.demo1.domain.daoimpl.UserDAOImpl;
import org.example.demo1.domain.model.User;
import org.example.demo1.common.security.JWTUtil;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.util.Base64;

@Path("/login")
public class LoginResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(User loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        try (Connection conn = DBUtil.getConnection()) {
            UserDAO userDAO = new UserDAOImpl(conn);
            User user = userDAO.getUserByUsername(username);

            if (user != null) {
                // Encode the entered password to match stored format
                String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());

                if (user.getPassword().equals(encodedPassword)) {
                    String token = JWTUtil.generateToken(user.getUsername(), user.getRole());
                    String json = "{\"token\":\"" + token + "\"}";
                    return Response.status(Response.Status.OK).entity(json).build();
                }
            }

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid username or password")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }
}
