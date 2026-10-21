package com.taller.dao;

import com.taller.model.Cliente;
import com.taller.model.Vehiculo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class VehiculoDao {
    private static final String SELECT = "SELECT v.placa, v.marca, v.modelo, v.anio, "
            + "c.id AS cid, c.nombre AS cnombre, c.telefono AS ctel, c.email AS cemail, c.direccion AS cdir "
            + "FROM vehiculo v JOIN cliente c ON c.id = v.cliente_id ";

    private final JdbcBase db;

    public VehiculoDao(JdbcBase db) {
        this.db = db;
    }

    static Vehiculo mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getString("cid"));
        c.setNombre(rs.getString("cnombre"));
        c.setTelefono(rs.getString("ctel"));
        c.setEmail(rs.getString("cemail"));
        c.setDireccion(rs.getString("cdir"));
        Vehiculo v = new Vehiculo();
        v.setPlaca(rs.getString("placa"));
        v.setMarca(rs.getString("marca"));
        v.setModelo(rs.getString("modelo"));
        v.setAnio(rs.getInt("anio"));
        v.setCliente(c);
        return v;
    }

    public List<Vehiculo> findAll() {
        return db.query(SELECT + "ORDER BY v.placa", VehiculoDao::mapear);
    }

    public Optional<Vehiculo> findByPlaca(String placa) {
        return db.queryOne(SELECT + "WHERE v.placa = ?", VehiculoDao::mapear, placa);
    }

    public void insert(Vehiculo v) {
        db.update("INSERT INTO vehiculo (placa, marca, modelo, anio, cliente_id) VALUES (?, ?, ?, ?, ?)",
                v.getPlaca(), v.getMarca(), v.getModelo(), v.getAnio(), v.getCliente().getId());
    }

    public void delete(String placa) {
        db.update("DELETE FROM vehiculo WHERE placa = ?", placa);
    }

    public int countByCliente(String clienteId) {
        return db.count("SELECT COUNT(*) FROM vehiculo WHERE cliente_id = ?", clienteId);
    }
}
