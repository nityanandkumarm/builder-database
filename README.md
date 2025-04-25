# 🏗️ Builder Database Service

A scalable and extensible Java + Spring Boot service for **dynamic database operations** via APIs.  
This project helps applications manage their PostgreSQL schemas, tables, indexes, and query behavior **at runtime** — with strong support for batching, metadata, and performance-tuned operations.

---

## ❓ Why Use This Service?

Managing evolving database schemas, optimizing data writes, and controlling metadata programmatically is **complex** and often application-specific. This service simplifies that by providing a **standardized, API-driven interface** to build, manage, and query relational structures at runtime.

### 🔍 Advantages

- **Schema Evolution Without Downtime**  
  Create or modify table structures dynamically via REST APIs — no manual SQL or migration scripts.

- **Performance via Temporary Writes**  
  High-throughput inserts/updates land in `TEXT`-typed temporary tables, bypassing serialization costs. Data is then flushed in batches to real tables.

- **Generic Select with Precision**  
  Return only required columns, include aggregated fields, and reduce network overhead.

- **Pluggable & Extensible**  
  Built with interchangeable SQL builders and clean interfaces — adaptable to non-PostgreSQL databases in future.

- **Clean Architecture**  
  Structured with separation between builder, service, mapper, and controller — ideal for SDK distribution or hosted client integration.

- **Metadata & Index Visibility**  
  Introspect schema or index info using JDBC metadata — great for admin dashboards or dynamic UIs.

---

## 🚀 Features

### ✅ Table & Schema Management
- Create actual and temporary write tables through API.
- Temporary write tables use all `TEXT` fields to eliminate serialization/deserialization.
- Built-in fields: `lastUpdateDate`, `isDeleted` (for soft deletes and batch cleanups).

### ✅ Index Support
- Request index creation on any combination of fields via API.
- Supports `BTREE`, `HASH`, etc. with unique constraints.

### ✅ Select Queries with Aggregates
- Retrieve only selected columns.
- Add rollups/aggregations (like `SUM`, `COUNT`, `AVG`) per column.
- Results returned via a flexible, generic DTO structure.

### ✅ Metadata Introspection
- Use API to fetch column-level and index metadata for any created table.
- Auto-detect primary keys and types from JDBC metadata.

### ✅ Safety & Validations
- DTO and model validations via JSR-303.
- Postgres-only type checking.
- SQL is generated using parameterized builders to avoid injection attacks.

---

## ⚙️ Tech Stack

- Java 17  
- Spring Boot 3.x  
- PostgreSQL  
- Maven  
- Lombok  
- HikariCP  
- JDBC Metadata APIs  
- Bean Validation (Jakarta)

---

## 📦 Installation

### 🔧 Requirements
- Java 17+
- PostgreSQL 13 or higher
- Maven 3.8+

### 🧱 Setup

1. **Clone the repo**

```bash
git clone https://github.com/your-username/builder-database-service.git
cd builder-database-service
```

2. **Configure database connection**

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database
    username: your_user
    password: your_password
  jpa:
    hibernate:
      ddl-auto: none
    open-in-view: false
```

3. **Build the project**

```bash
mvn clean install
```

4. **Run the application**

```bash
mvn spring-boot:run
```

The service will be available at: `http://localhost:8080`.

---

## 🧪 Example Workflow

1. **POST** `/api/tables/create`  
   Creates schema + actual + temporary write table (if enabled).

2. **POST** `/api/tables/select`  
   Execute select queries with filters and aggregates.

3. **POST** `/api/tables/{schema}/{table}/indexes`  
   Add indexes to an existing table.

4. **GET** `/api/tables/{schema}/{table}?includeIndexes=true`  
   Fetch metadata + optional indexes.

5. **POST** `/api/tables/flush`  
   Flush data from temporary table → actual table.

---

## 🛣️ Roadmap

- [x] Table & temp write table creation
- [x] Indexing support
- [x] Generic select with filters and rollups
- [x] Metadata API
- [x] Insert API → writes to temp or actual table based on context
- [x] Flush API → batch-based transfer to actual table
- [ ] Cron job for automated flush
- [ ] Soft delete cleanup jobs
- [ ] View & Materialized View creation
- [ ] Caching for metadata and schema
- [ ] Multi-tenant schema isolation

---

## 🧑‍💻 Contributing

Pull requests are welcome! Please fork the repo and open a PR from a feature branch.  
If you're not sure how to approach a feature, open an issue first to discuss.

---

## 📄 License

```
Apache License 2.0

Licensed under the Apache License, Version 2.0 (the "License");  
you may not use this file except in compliance with the License.  
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software  
distributed under the License is distributed on an "AS IS" BASIS,  
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
See the License for the specific language governing permissions and  
limitations under the License.
```
