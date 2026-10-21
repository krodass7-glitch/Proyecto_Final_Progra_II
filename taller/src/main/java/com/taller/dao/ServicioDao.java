package com.taller.dao;

import com.taller.model.Servicio;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ServicioDao {
    private static final String SELECT = "SELECT id, descripcion, costo_mano_obra FROM servicio ";

    private final JdbcBase db;

    public ServicioDao(JdbcBase db) {
        this.db = db;
    }

    static Servicio mapear(ResultSet rs) throws SQLException {
        Servicio s = new Servicio();
        s.setId(rs.getString("id"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setCostoManoObra(rs.getBigDecimal("costo_mano_obra"));
        return s;
    }

    public List<Servicio> findAll() {
        return db.query(SELECT + "ORDER BY descripcion", ServicioDao::mapear);
    }

    public Optional<Servicio> findById(String id) {
        return db.queryOne(SELECT + "WHERE id = ?", ServicioDao::mapear, id);
    }

    public void insert(Servicio s) {
        db.update("INSERT INTO servicio (id, descripcion, costo_mano_obra) VALUES (?, ?, ?)",
                s.getId(), s.getDescripcion(), s.getCostoManoObra());
    }

    public void update(Servicio s) {
        db.update("UPDATE servicio SET descripcion = ?, costo_mano_obra = ? WHERE id = ?",
                s.getDescripcion(), s.getCostoManoObra(), s.getId());
    }

    public void delete(String id) {
        db.update("DELETE FROM servicio WHERE id = ?", id);
    }
}
