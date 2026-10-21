package com.taller.dao;

import com.taller.exception.NegocioException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

@Component
public class JdbcBase {
    private final DataSource dataSource;

    public JdbcBase(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @FunctionalInterface
    public interface Mapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public int update(String sql, Object... params) {
        Connection conexion = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            enlazar(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw traducir(e);
        } finally {
            DataSourceUtils.releaseConnection(conexion, dataSource);
        }
    }

    public int insertarConId(String sql, Object... params) {
        Connection conexion = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            enlazar(ps, params);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                return claves.next() ? claves.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw traducir(e);
        } finally {
            DataSourceUtils.releaseConnection(conexion, dataSource);
        }
    }

    public <T> List<T> query(String sql, Mapper<T> mapper, Object... params) {
        Connection conexion = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            enlazar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapper.map(rs));
                }
                return resultado;
            }
        } catch (SQLException e) {
            throw traducir(e);
        } finally {
            DataSourceUtils.releaseConnection(conexion, dataSource);
        }
    }

    public <T> Optional<T> queryOne(String sql, Mapper<T> mapper, Object... params) {
        return query(sql, mapper, params).stream().findFirst();
    }

    public int count(String sql, Object... params) {
        return queryOne(sql, rs -> rs.getInt(1), params).orElse(0);
    }

    private void enlazar(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object valor = params[i];
            if (valor instanceof LocalDateTime fecha) {
                valor = Timestamp.valueOf(fecha);
            } else if (valor instanceof Enum<?> enumerado) {
                valor = enumerado.name();
            }
            ps.setObject(i + 1, valor);
        }
    }

    private NegocioException traducir(SQLException e) {
        return switch (e.getErrorCode()) {
            case 1062 -> new NegocioException("Ya existe un registro con ese identificador");
            case 1451 -> new NegocioException("No se puede eliminar: el registro tiene información asociada");
            case 1452 -> new NegocioException("La referencia indicada no existe");
            case 3819 -> new NegocioException("Valor fuera del rango permitido");
            default -> new NegocioException("Error de base de datos: " + e.getMessage());
        };
    }
}
