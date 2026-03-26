# Cucumber Web v2

Aplicación full-stack para gestionar archivos `.feature` de Cucumber con soporte de `Scenario Outline` y múltiples bloques `Examples`.

## Stack

- **Backend:** Java 17 + Spring Boot 3
- **Frontend:** Vue 3 + Composition API + TypeScript + Vite + Pinia + Vue Router

## Estructura

- `backend/`: API REST, parser de Gherkin, validador y exportador.
- `frontend/`: UI moderna tipo dashboard con editor visual de escenarios y examples.
- `features/`: carpeta por defecto para importar/exportar `.feature`.

## Requisitos

- Java 17+
- Maven 3.9+
- Node 20+

## Arranque

### Backend

```bash
cd backend
mvn spring-boot:run
```

Configurar ruta de features:

- `application.yml`:

```yaml
app:
  cucumber:
    features-path: ../features
```

- o variable de entorno:

```bash
export CUCUMBER_FEATURES_PATH=./features
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend en `http://localhost:5173` y backend en `http://localhost:8080`.

## API principal

- `GET /api/features`
- `GET /api/features/{id}`
- `POST /api/features`
- `PUT /api/features/{id}`
- `DELETE /api/features/{id}`
- `POST /api/features/{id}/save`
- `GET /api/features/{id}/export`
- `POST /api/import/rescan`
- `GET /api/settings/features-path`

## Funcionalidades MVP

- Lectura real de `.feature` desde disco.
- Parseo estructurado de `Feature`, `Background`, `Scenario`, `Scenario Outline`, tags y múltiples `Examples`.
- Editor visual para escenarios, steps y tablas de examples (filas/columnas/celdas inline).
- Validación backend de placeholders `<param>` vs columnas de examples.
- Preview Gherkin en tiempo real.
- Guardado/exportación a `.feature` legible.

## Tests

Backend incluye pruebas unitarias de parser, validador y servicio:

```bash
cd backend
mvn test
```

## Decisiones técnicas

- Parser propio mantenible por estados (evita regex frágil para el documento completo).
- Dominio separado de DTOs con mapeador dedicado.
- Exportador centralizado para garantizar formato consistente.
- UI dividida por componentes reutilizables (`FeatureSidebar`, `ScenarioEditor`, `ExamplesTableEditor`, `GherkinPreview`).
