package com.taller.dao;

import com.taller.model.Cliente;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteDao {
    private final JdbcBase db;

    public ClienteDao(JdbcBase db) {
        this.db = db;
    }

    static Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getString("id"));
        c.setNombre(rs.getString("nombre"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setDireccion(rs.getString("direccion"));
        return c;
    }

    public List<Cliente> findAll() {
        return db.query("SELECT id, nombre, telefono, email, direccion FROM cliente ORDER BY nombre", ClienteDao::mapear);
    }

    public Optional<Cliente> findById(String id) {
        return db.queryOne("SELECT id, nombre, telefono, email, direccion FROM cliente WHERE id = ?", ClienteDao::mapear, id);
    }

    public void insert(Cliente c) {
        db.update("INSERT INTO cliente (id, nombre, telefono, email, direccion) VALUES (?, ?, ?, ?, ?)",
                c.getId(), c.getNombre(), c.getTelefono(), c.getEmail(), c.getDireccion());
    }

    public void update(Cliente c) {
        db.update("UPDATE cliente SET nombre = ?, telefono = ?, email = ?, direccion = ? WHERE id = ?",
                c.getNombre(), c.getTelefono(), c.getEmail(), c.getDireccion(), c.getId());
    }

    public void delete(String id) {
        db.update("DELETE FROM cliente WHERE id = ?", id);
    }
}
