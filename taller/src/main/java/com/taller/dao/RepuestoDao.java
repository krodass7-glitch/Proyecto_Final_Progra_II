package com.taller.dao;

import com.taller.model.Repuesto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RepuestoDao {
    private static final String SELECT = "SELECT id, nombre, descripcion, precio, existencia FROM repuesto ";

    private final JdbcBase db;

    public RepuestoDao(JdbcBase db) {
        this.db = db;
    }

    static Repuesto mapear(ResultSet rs) throws SQLException {
        Repuesto r = new Repuesto();
        r.setId(rs.getString("id"));
        r.setNombre(rs.getString("nombre"));
        r.setDescripcion(rs.getString("descripcion"));
        r.setPrecio(rs.getBigDecimal("precio"));
        r.setExistencia(rs.getInt("existencia"));
        return r;
    }

    public List<Repuesto> findAll() {
        return db.query(SELECT + "ORDER BY nombre", RepuestoDao::mapear);
    }

    public List<Repuesto> findByExistenciaMaxima(int limite) {
        return db.query(SELECT + "WHERE existencia <= ? ORDER BY existencia, nombre", RepuestoDao::mapear, limite);
    }

    public Optional<Repuesto> findById(String id) {
        return db.queryOne(SELECT + "WHERE id = ?", RepuestoDao::mapear, id);
    }

    public void insert(Repuesto r) {
        db.update("INSERT INTO repuesto (id, nombre, descripcion, precio, existencia) VALUES (?, ?, ?, ?, ?)",
                r.getId(), r.getNombre(), r.getDescripcion(), r.getPrecio(), r.getExistencia());
    }

    public void update(Repuesto r) {
        db.update("UPDATE repuesto SET nombre = ?, descripcion = ?, precio = ?, existencia = ? WHERE id = ?",
                r.getNombre(), r.getDescripcion(), r.getPrecio(), r.getExistencia(), r.getId());
    }

    public void abastecer(String id, int cantidad) {
        db.update("UPDATE repuesto SET existencia = existencia + ? WHERE id = ?", cantidad, id);
    }

    public int descontar(String id, int cantidad) {
        return db.update("UPDATE repuesto SET existencia = existencia - ? WHERE id = ? AND existencia >= ?",
                cantidad, id, cantidad);
    }

    public void reintegrar(String id, int cantidad) {
        abastecer(id, cantidad);
    }
}
