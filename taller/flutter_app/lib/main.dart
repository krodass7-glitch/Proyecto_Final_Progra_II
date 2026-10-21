import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

void main() => runApp(const TallerApp());

class TallerApp extends StatelessWidget {
  const TallerApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'El Último Arranque',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorSchemeSeed: const Color(0xFF1F3A42),
        useMaterial3: true,
      ),
      home: const PantallaReportes(),
    );
  }
}

class TipoReporte {
  final String clave;
  final String etiqueta;
  const TipoReporte(this.clave, this.etiqueta);
}

const tiposReporte = [
  TipoReporte('estados', 'Órdenes por estado'),
  TipoReporte('repuestos', 'Repuestos con existencia ≤ 5'),
  TipoReporte('mecanicos', 'Órdenes por mecánico y estado'),
  TipoReporte('vehiculo', 'Historial por vehículo (placa)'),
  TipoReporte('ingresos', 'Ingresos por rango de fechas'),
];

class Reporte {
  final String titulo;
  final List<String> columnas;
  final List<List<String>> filas;
  final String? pie;

  Reporte(this.titulo, this.columnas, this.filas, this.pie);

  factory Reporte.desdeJson(Map<String, dynamic> json) {
    return Reporte(
      json['titulo'] as String,
      (json['columnas'] as List).map((c) => c.toString()).toList(),
      (json['filas'] as List)
          .map((f) => (f as List).map((v) => v.toString()).toList())
          .toList(),
      json['pie'] as String?,
    );
  }
}

class PantallaReportes extends StatefulWidget {
  const PantallaReportes({super.key});

  @override
  State<PantallaReportes> createState() => _PantallaReportesState();
}

class _PantallaReportesState extends State<PantallaReportes> {
  final servidor = TextEditingController(text: 'http://localhost:8080');
  final placa = TextEditingController();
  TipoReporte tipo = tiposReporte.first;
  DateTime? desde;
  DateTime? hasta;
  bool cargando = false;
  String? error;
  Reporte? reporte;

  String formatoFecha(DateTime f) =>
      '${f.year}-${f.month.toString().padLeft(2, '0')}-${f.day.toString().padLeft(2, '0')}';

  Future<void> elegirFecha(bool esDesde) async {
    final ahora = DateTime.now();
    final elegida = await showDatePicker(
      context: context,
      initialDate: (esDesde ? desde : hasta) ?? ahora,
      firstDate: DateTime(2020),
      lastDate: DateTime(2100),
    );
    if (elegida != null) {
      setState(() => esDesde ? desde = elegida : hasta = elegida);
    }
  }

  Future<void> consultar() async {
    setState(() {
      cargando = true;
      error = null;
      reporte = null;
    });
    try {
      final parametros = <String, String>{};
      if (tipo.clave == 'vehiculo') {
        parametros['placa'] = placa.text.trim();
      }
      if (tipo.clave == 'ingresos') {
        if (desde == null || hasta == null) {
          throw Exception('Seleccione la fecha inicial y la final');
        }
        parametros['desde'] = formatoFecha(desde!);
        parametros['hasta'] = formatoFecha(hasta!);
      }
      final uri = Uri.parse(servidor.text.trim()).replace(
        path: '/api/reportes/${tipo.clave}',
        queryParameters: parametros.isEmpty ? null : parametros,
      );
      final respuesta = await http.get(uri).timeout(const Duration(seconds: 8));
      final cuerpo = jsonDecode(utf8.decode(respuesta.bodyBytes));
      if (respuesta.statusCode >= 400) {
        throw Exception(cuerpo['error'] ?? 'Error del servidor');
      }
      setState(() => reporte = Reporte.desdeJson(cuerpo as Map<String, dynamic>));
    } catch (e) {
      setState(() => error = e.toString().replaceFirst('Exception: ', ''));
    } finally {
      setState(() => cargando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('El Último Arranque')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                TextField(
                  controller: servidor,
                  keyboardType: TextInputType.url,
                  decoration: const InputDecoration(
                    labelText: 'Dirección del servidor',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<TipoReporte>(
                  initialValue: tipo,
                  decoration: const InputDecoration(
                    labelText: 'Reporte',
                    border: OutlineInputBorder(),
                  ),
                  items: [
                    for (final t in tiposReporte)
                      DropdownMenuItem(value: t, child: Text(t.etiqueta)),
                  ],
                  onChanged: (t) => setState(() => tipo = t!),
                ),
                if (tipo.clave == 'vehiculo') ...[
                  const SizedBox(height: 12),
                  TextField(
                    controller: placa,
                    textCapitalization: TextCapitalization.characters,
                    decoration: const InputDecoration(
                      labelText: 'Placa (ej. P-123ABC)',
                      border: OutlineInputBorder(),
                    ),
                  ),
                ],
                if (tipo.clave == 'ingresos') ...[
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton(
                          onPressed: () => elegirFecha(true),
                          child: Text(desde == null ? 'Desde' : formatoFecha(desde!)),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: OutlinedButton(
                          onPressed: () => elegirFecha(false),
                          child: Text(hasta == null ? 'Hasta' : formatoFecha(hasta!)),
                        ),
                      ),
                    ],
                  ),
                ],
                const SizedBox(height: 12),
                FilledButton(
                  onPressed: cargando ? null : consultar,
                  child: const Text('Consultar reporte'),
                ),
              ],
            ),
          ),
          Expanded(child: resultado()),
        ],
      ),
    );
  }

  Widget resultado() {
    if (cargando) {
      return const Center(child: CircularProgressIndicator());
    }
    if (error != null) {
      return Padding(
        padding: const EdgeInsets.all(16),
        child: Text(error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
      );
    }
    final r = reporte;
    if (r == null) {
      return const Center(child: Text('Elija un reporte y pulse Consultar'));
    }
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
      children: [
        Text(r.titulo, style: Theme.of(context).textTheme.titleMedium),
        const SizedBox(height: 8),
        if (r.filas.isEmpty) const Text('No hay datos para este reporte.'),
        for (final fila in r.filas)
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  for (var i = 0; i < r.columnas.length; i++)
                    Text.rich(TextSpan(children: [
                      TextSpan(
                        text: '${r.columnas[i]}: ',
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                      TextSpan(text: fila[i]),
                    ])),
                ],
              ),
            ),
          ),
        if (r.pie != null)
          Padding(
            padding: const EdgeInsets.only(top: 8),
            child: Text(r.pie!, style: const TextStyle(fontWeight: FontWeight.bold)),
          ),
      ],
    );
  }
}
