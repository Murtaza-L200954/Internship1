package org.example.demo1.domain.daoimpl;

import org.example.demo1.domain.dao.ProductDAO;
import org.example.demo1.domain.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDAOImpl implements ProductDAO {
    private final Connection conn;
    private static final Logger log = LoggerFactory.getLogger(ProductDAOImpl.class);

    public ProductDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Product> getAllProducts(){
        List<Product> products = new ArrayList<>();
        String sql = "select * from products";

        try(PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                products.add(product);
            }
            log.info("Retrieved {} products successfully", products.size());
        }
        catch (SQLException e){
            log.error("Error retrieving products from DB", e);
        }
        return products;
    }

    @Override
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setId(id);
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getDouble("price"));
                    product.setStock(rs.getInt("stock"));
                    return product;
                }
                log.info("Retrieved {} product successfully", id);
            }
        } catch (SQLException e) {
            log.error("Error retrieving product from DB", e);
        }
        return null;
    }

    @Override
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (id, name, description, price, stock) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            log.info("Added product {} successfully", product.getName());
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e) {
            log.error("Error adding product to DB", e);
            return false;
        }
    }

    @Override
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setInt(5, product.getId());
            log.info("Updated product {} successfully", product.getName());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating product from DB", e);
            return false;
        }
    }

    @Override
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            log.info("Deleted product {} successfully", id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting product from DB", e);
            return false;
        }
    }
}
