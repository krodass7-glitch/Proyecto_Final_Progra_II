package com.taller.dao;

import com.taller.model.Comprobante;
import com.taller.model.DetalleRepuesto;
import com.taller.model.DetalleServicio;
import com.taller.model.EstadoOrden;
import com.taller.model.Mecanico;
import com.taller.model.OrdenTrabajo;
import com.taller.model.Servicio;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OrdenDao {
    private static final String SELECT = "SELECT o.id, o.descripcion_problema, o.estado, o.fecha_creacion, "
            + "o.fecha_finalizacion, o.costo_total, "
            + "v.placa, v.marca, v.modelo, v.anio, "
            + "c.id AS cid, c.nombre AS cnombre, c.telefono AS ctel, c.email AS cemail, c.direccion AS cdir, "
            + "m.id AS mid, m.nombre AS mnombre, m.telefono AS mtel, m.especialidad AS mesp "
            + "FROM orden_trabajo o "
            + "JOIN vehiculo v ON v.placa = o.placa "
            + "JOIN cliente c ON c.id = v.cliente_id "
            + "JOIN mecanico m ON m.id = o.mecanico_id ";

    private final JdbcBase db;

    public OrdenDao(JdbcBase db) {
        this.db = db;
    }

    private static LocalDateTime fecha(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }

    private static OrdenTrabajo mapear(ResultSet rs) throws SQLException {
        Mecanico m = new Mecanico();
        m.setId(rs.getString("mid"));
        m.setNombre(rs.getString("mnombre"));
        m.setTelefono(rs.getString("mtel"));
        m.setEspecialidad(rs.getString("mesp"));
        OrdenTrabajo o = new OrdenTrabajo();
        o.setId(rs.getInt("id"));
        o.setVehiculo(VehiculoDao.mapear(rs));
        o.setMecanico(m);
        o.setDescripcionProblema(rs.getString("descripcion_problema"));
        o.setEstado(EstadoOrden.valueOf(rs.getString("estado")));
        o.setFechaCreacion(fecha(rs.getTimestamp("fecha_creacion")));
        o.setFechaFinalizacion(fecha(rs.getTimestamp("fecha_finalizacion")));
        o.setCostoTotal(rs.getBigDecimal("costo_total"));
        return o;
    }

    public int insert(String placa, String mecanicoId, String descripcion) {
        return db.insertarConId("INSERT INTO orden_trabajo (placa, mecanico_id, descripcion_problema, estado, fecha_creacion) "
                + "VALUES (?, ?, ?, ?, ?)", placa, mecanicoId, descripcion, EstadoOrden.PENDIENTE, LocalDateTime.now());
    }

    public List<OrdenTrabajo> findAll() {
        return db.query(SELECT + "ORDER BY o.id DESC", OrdenDao::mapear);
    }

    public List<OrdenTrabajo> findByEstado(EstadoOrden estado) {
        return db.query(SELECT + "WHERE o.estado = ? ORDER BY o.id DESC", OrdenDao::mapear, estado);
    }

    public List<OrdenTrabajo> findByPlaca(String placa) {
        return db.query(SELECT + "WHERE o.placa = ? ORDER BY o.fecha_creacion DESC", OrdenDao::mapear, placa);
    }

    public List<OrdenTrabajo> findFinalizadasEntre(LocalDateTime desde, LocalDateTime hasta) {
        return db.query(SELECT + "WHERE o.estado = ? AND o.fecha_finalizacion >= ? AND o.fecha_finalizacion < ? "
                + "ORDER BY o.fecha_finalizacion", OrdenDao::mapear, EstadoOrden.FINALIZADA, desde, hasta);
    }

    public Optional<OrdenTrabajo> findById(int id) {
        Optional<OrdenTrabajo> orden = db.queryOne(SELECT + "WHERE o.id = ?", OrdenDao::mapear, id);
        orden.ifPresent(o -> {
            o.setServicios(serviciosDe(id));
            o.setRepuestos(repuestosDe(id));
        });
        return orden;
    }

    public int countByMecanico(String mecanicoId) {
        return db.count("SELECT COUNT(*) FROM orden_trabajo WHERE mecanico_id = ?", mecanicoId);
    }

    public int countByPlaca(String placa) {
        return db.count("SELECT COUNT(*) FROM orden_trabajo WHERE placa = ?", placa);
    }

    public void cambiarEstado(int id, EstadoOrden estado) {
        db.update("UPDATE orden_trabajo SET estado = ? WHERE id = ?", estado, id);
    }

    public void finalizar(int id, BigDecimal total, LocalDateTime fecha) {
        db.update("UPDATE orden_trabajo SET estado = ?, costo_total = ?, fecha_finalizacion = ? WHERE id = ?",
                EstadoOrden.FINALIZADA, total, fecha, id);
    }

    public List<DetalleServicio> serviciosDe(int ordenId) {
        return db.query("SELECT s.id, s.descripcion, s.costo_mano_obra, os.costo FROM orden_servicio os "
                + "JOIN servicio s ON s.id = os.servicio_id WHERE os.orden_id = ? ORDER BY os.id", rs -> {
            Servicio s = ServicioDao.mapear(rs);
            return new DetalleServicio(s, rs.getBigDecimal("costo"));
        }, ordenId);
    }

    public List<DetalleRepuesto> repuestosDe(int ordenId) {
        return db.query("SELECT r.id, r.nombre, r.descripcion, r.precio, r.existencia, orp.cantidad, orp.precio_unitario "
                + "FROM orden_repuesto orp JOIN repuesto r ON r.id = orp.repuesto_id "
                + "WHERE orp.orden_id = ? ORDER BY orp.id", rs ->
                new DetalleRepuesto(RepuestoDao.mapear(rs), rs.getInt("cantidad"), rs.getBigDecimal("precio_unitario")), ordenId);
    }

    public int contarServicio(int ordenId, String servicioId) {
        return db.count("SELECT COUNT(*) FROM orden_servicio WHERE orden_id = ? AND servicio_id = ?", ordenId, servicioId);
    }

    public void insertServicio(int ordenId, String servicioId, BigDecimal costo) {
        db.update("INSERT INTO orden_servicio (orden_id, servicio_id, costo) VALUES (?, ?, ?)", ordenId, servicioId, costo);
    }

    public void deleteServicio(int ordenId, String servicioId) {
        db.update("DELETE FROM orden_servicio WHERE orden_id = ? AND servicio_id = ?", ordenId, servicioId);
    }

    public Optional<Integer> cantidadAsignada(int ordenId, String repuestoId) {
        return db.queryOne("SELECT cantidad FROM orden_repuesto WHERE orden_id = ? AND repuesto_id = ?",
                rs -> rs.getInt("cantidad"), ordenId, repuestoId);
    }

    public void insertRepuesto(int ordenId, String repuestoId, int cantidad, BigDecimal precio) {
        db.update("INSERT INTO orden_repuesto (orden_id, repuesto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)",
                ordenId, repuestoId, cantidad, precio);
    }

    public void sumarCantidad(int ordenId, String repuestoId, int cantidad) {
        db.update("UPDATE orden_repuesto SET cantidad = cantidad + ? WHERE orden_id = ? AND repuesto_id = ?",
                cantidad, ordenId, repuestoId);
    }

    public void deleteRepuesto(int ordenId, String repuestoId) {
        db.update("DELETE FROM orden_repuesto WHERE orden_id = ? AND repuesto_id = ?", ordenId, repuestoId);
    }

    public void deleteRepuestos(int ordenId) {
        db.update("DELETE FROM orden_repuesto WHERE orden_id = ?", ordenId);
    }

    public void insertComprobante(Comprobante c) {
        db.update("INSERT INTO comprobante (orden_id, fecha, total_servicios, total_repuestos, total) VALUES (?, ?, ?, ?, ?)",
                c.getOrdenId(), c.getFecha(), c.getTotalServicios(), c.getTotalRepuestos(), c.getTotal());
    }

    public Optional<Comprobante> findComprobante(int ordenId) {
        return db.queryOne("SELECT id, orden_id, fecha, total_servicios, total_repuestos, total FROM comprobante WHERE orden_id = ?", rs -> {
            Comprobante c = new Comprobante();
            c.setId(rs.getInt("id"));
            c.setOrdenId(rs.getInt("orden_id"));
            c.setFecha(fecha(rs.getTimestamp("fecha")));
            c.setTotalServicios(rs.getBigDecimal("total_servicios"));
            c.setTotalRepuestos(rs.getBigDecimal("total_repuestos"));
            c.setTotal(rs.getBigDecimal("total"));
            return c;
        }, ordenId);
    }
}
