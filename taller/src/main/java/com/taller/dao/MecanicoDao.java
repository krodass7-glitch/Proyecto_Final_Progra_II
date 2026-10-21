package com.taller.dao;

import com.taller.model.Mecanico;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MecanicoDao {
    private final JdbcBase db;

    public MecanicoDao(JdbcBase db) {
        this.db = db;
    }

    static Mecanico mapear(ResultSet rs) throws SQLException {
        Mecanico m = new Mecanico();
        m.setId(rs.getString("id"));
        m.setNombre(rs.getString("nombre"));
        m.setTelefono(rs.getString("telefono"));
        m.setEspecialidad(rs.getString("especialidad"));
        return m;
    }

    public List<Mecanico> findAll() {
        return db.query("SELECT id, nombre, telefono, especialidad FROM mecanico ORDER BY nombre", MecanicoDao::mapear);
    }

    public Optional<Mecanico> findById(String id) {
        return db.queryOne("SELECT id, nombre, telefono, especialidad FROM mecanico WHERE id = ?", MecanicoDao::mapear, id);
    }

    public void insert(Mecanico m) {
        db.update("INSERT INTO mecanico (id, nombre, telefono, especialidad) VALUES (?, ?, ?, ?)",
                m.getId(), m.getNombre(), m.getTelefono(), m.getEspecialidad());
    }

    public void update(Mecanico m) {
        db.update("UPDATE mecanico SET nombre = ?, telefono = ?, especialidad = ? WHERE id = ?",
                m.getNombre(), m.getTelefono(), m.getEspecialidad(), m.getId());
    }

    public void delete(String id) {
        db.update("DELETE FROM mecanico WHERE id = ?", id);
    }
}
