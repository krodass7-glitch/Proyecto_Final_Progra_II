CREATE DATABASE taller_mecanico ;
USE taller_mecanico;

CREATE TABLE cliente (
    id VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(200)
);

CREATE TABLE vehiculo (
    placa VARCHAR(15) PRIMARY KEY,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    anio INT NOT NULL,
    cliente_id VARCHAR(20) NOT NULL,
    CONSTRAINT fk_vehiculo_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE TABLE mecanico (
    id VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    especialidad VARCHAR(100)
);

CREATE TABLE repuesto (
    id VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    precio DECIMAL(10,2) NOT NULL,
    existencia INT NOT NULL,
    CONSTRAINT ck_repuesto_precio CHECK (precio >= 0),
    CONSTRAINT ck_repuesto_existencia CHECK (existencia >= 0)
);

CREATE TABLE servicio (
    id VARCHAR(20) PRIMARY KEY,
    descripcion VARCHAR(150) NOT NULL,
    costo_mano_obra DECIMAL(10,2) NOT NULL,
    CONSTRAINT ck_servicio_costo CHECK (costo_mano_obra >= 0)
);

CREATE TABLE orden_trabajo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(15) NOT NULL,
    mecanico_id VARCHAR(20) NOT NULL,
    descripcion_problema TEXT NOT NULL,
    estado VARCHAR(15) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_finalizacion DATETIME NULL,
    costo_total DECIMAL(12,2) NULL,
    CONSTRAINT fk_orden_vehiculo FOREIGN KEY (placa) REFERENCES vehiculo(placa),
    CONSTRAINT fk_orden_mecanico FOREIGN KEY (mecanico_id) REFERENCES mecanico(id),
    CONSTRAINT ck_orden_estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','FINALIZADA','CANCELADA'))
);

CREATE TABLE orden_servicio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orden_id INT NOT NULL,
    servicio_id VARCHAR(20) NOT NULL,
    costo DECIMAL(10,2) NOT NULL,
    CONSTRAINT uq_orden_servicio UNIQUE (orden_id, servicio_id),
    CONSTRAINT fk_os_orden FOREIGN KEY (orden_id) REFERENCES orden_trabajo(id),
    CONSTRAINT fk_os_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id)
);

CREATE TABLE orden_repuesto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orden_id INT NOT NULL,
    repuesto_id VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT uq_orden_repuesto UNIQUE (orden_id, repuesto_id),
    CONSTRAINT ck_or_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_or_orden FOREIGN KEY (orden_id) REFERENCES orden_trabajo(id),
    CONSTRAINT fk_or_repuesto FOREIGN KEY (repuesto_id) REFERENCES repuesto(id)
);

CREATE TABLE comprobante (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orden_id INT NOT NULL UNIQUE,
    fecha DATETIME NOT NULL,
    total_servicios DECIMAL(12,2) NOT NULL,
    total_repuestos DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_comprobante_orden FOREIGN KEY (orden_id) REFERENCES orden_trabajo(id)
);

INSERT INTO cliente VALUES
('CL-001','Juan Pérez','5551-1001','juan.perez@correo.com','Zona 1, Guatemala'),
('CL-002','María López','5551-1002','maria.lopez@correo.com','Zona 10, Guatemala'),
('CL-003','Pedro Gómez','5551-1003','pedro.gomez@correo.com','Mixco'),
('CL-004','Lucía Herrera','5551-1004','lucia.herrera@correo.com','Villa Nueva'),
('CL-005','Andrés Morales','5551-1005','andres.morales@correo.com','Zona 7, Guatemala');

INSERT INTO vehiculo VALUES
('P-123ABC','Toyota','Corolla',2015,'CL-001'),
('P-456DEF','Honda','Civic',2018,'CL-002'),
('P-789GHI','Nissan','Sentra',2012,'CL-003'),
('P-321JKL','Mazda','3',2020,'CL-001'),
('P-654MNO','Hyundai','Accent',2016,'CL-004'),
('P-987PQR','Suzuki','Swift',2019,'CL-005');

INSERT INTO mecanico VALUES
('M-001','Carlos Méndez','5552-2001','Motor y transmisión'),
('M-002','Luis Ramírez','5552-2002','Frenos y suspensión'),
('M-003','Ana Castillo','5552-2003','Electricidad automotriz');

INSERT INTO repuesto VALUES
('R-001','Filtro de aceite','Filtro de aceite universal',45.00,25),
('R-002','Aceite 10W-30','Aceite sintético por litro',55.00,40),
('R-003','Pastillas de freno','Juego delantero cerámico',280.00,8),
('R-004','Bujía NGK','Bujía de iridio',38.50,4),
('R-005','Batería 12V 45Ah','Batería libre de mantenimiento',750.00,3),
('R-006','Filtro de aire','Filtro de aire de motor',65.00,12),
('R-007','Disco de freno','Disco ventilado delantero',420.00,0),
('R-008','Banda de distribución','Banda con kit de tensores',310.00,5);

INSERT INTO servicio VALUES
('S-001','Cambio de aceite',150.00),
('S-002','Alineación y balanceo',200.00),
('S-003','Revisión de frenos',120.00),
('S-004','Cambio de batería',80.00),
('S-005','Diagnóstico con escáner',175.00),
('S-006','Cambio de bujías',130.00);

INSERT INTO orden_trabajo (id,placa,mecanico_id,descripcion_problema,estado,fecha_creacion,fecha_finalizacion,costo_total) VALUES
(1,'P-123ABC','M-001','Mantenimiento de 5,000 km','FINALIZADA','2026-10-20 08:30:00','2026-10-20 11:00:00',415.00),
(2,'P-456DEF','M-002','Ruido al frenar','FINALIZADA','2026-10-27 09:00:00','2026-10-27 13:15:00',400.00),
(3,'P-789GHI','M-001','El motor falla en ralentí','EN_PROCESO','2026-11-04 10:00:00',NULL,NULL),
(4,'P-321JKL','M-003','Revisión del sistema eléctrico','PENDIENTE','2026-11-05 15:20:00',NULL,NULL),
(5,'P-123ABC','M-002','Vibración en el volante','CANCELADA','2026-11-02 11:45:00',NULL,NULL),
(6,'P-654MNO','M-003','La batería no retiene carga','FINALIZADA','2026-11-03 08:10:00','2026-11-03 10:40:00',1005.00);

INSERT INTO orden_servicio (orden_id,servicio_id,costo) VALUES
(1,'S-001',150.00),
(2,'S-003',120.00),
(3,'S-006',130.00),
(6,'S-004',80.00),
(6,'S-005',175.00);

INSERT INTO orden_repuesto (orden_id,repuesto_id,cantidad,precio_unitario) VALUES
(1,'R-001',1,45.00),
(1,'R-002',4,55.00),
(2,'R-003',1,280.00),
(3,'R-004',2,38.50),
(6,'R-005',1,750.00);

INSERT INTO comprobante (orden_id,fecha,total_servicios,total_repuestos,total) VALUES
(1,'2026-10-20 11:00:00',150.00,265.00,415.00),
(2,'2026-10-27 13:15:00',120.00,280.00,400.00),
(6,'2026-11-03 10:40:00',255.00,750.00,1005.00);
