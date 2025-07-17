package org.example.demo1.domain.daoimpl;

import org.example.demo1.common.LogUtil;
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
            LogUtil.logInfo(log, "Get all products successful " + products);
        }
        catch (SQLException e){
            LogUtil.logError(log, "Failed to get all products " + products, e);
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
                    LogUtil.setMDC(id); // I used product id here since I dont understand how to use userid here for now .
                    return product;
                }
                LogUtil.logInfo(log, "Get product by id " + id);
            }
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to get product by id " + id, e);
        } finally {
            LogUtil.clearMDC();
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
            LogUtil.setMDC(product.getId());
            LogUtil.logInfo(log, "Add product successful " + product);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e) {
            LogUtil.logError(log, "Failed to add product " + product, e);
            return false;
        } finally{
            LogUtil.clearMDC();
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
            LogUtil.setMDC(product.getId());
            LogUtil.logInfo(log, "Update product successful " + product);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to update product " + product, e);
            return false;
        } finally{
            LogUtil.clearMDC();
        }
    }

    @Override
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            LogUtil.setMDC(id);
            LogUtil.logInfo(log, "Delete product successful " + id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LogUtil.logError(log, "Failed to delete product " + id, e);
            return false;
        } finally{
            LogUtil.clearMDC();
        }
    }
}
