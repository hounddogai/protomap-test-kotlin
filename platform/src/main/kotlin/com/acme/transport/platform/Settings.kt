package com.acme.transport.platform

import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

/** Deployment settings come from the sidecar and database bindings supplied by the platform team. */
data class Settings(
    val port: Int,
    val meshTarget: String,
    val identityTarget: String,
    val jdbcUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    /** The workload credential the server makes its own calls with, such as its identity lookups. */
    val serviceToken: String = "",
) {
    fun dataSource(): DataSource = PGSimpleDataSource().apply {
        setURL(jdbcUrl)
        user = databaseUser
        password = databasePassword
        connectTimeout = 5
    }

    companion object {
        fun fromEnvironment(env: Map<String, String> = System.getenv()): Settings = Settings(
            port = env["GRPC_PORT"]?.toIntOrNull() ?: 8443,
            meshTarget = env["MESH_TARGET"] ?: "localhost:15001",
            identityTarget = env["IDENTITY_TARGET"] ?: "localhost:15002",
            jdbcUrl = env["JDBC_URL"] ?: "jdbc:postgresql://localhost:5432/acme",
            databaseUser = env["DATABASE_USER"] ?: "acme",
            databasePassword = env["DATABASE_PASSWORD"].orEmpty(),
            serviceToken = env["ACME_SERVICE_TOKEN"].orEmpty(),
        ).also { require(it.port in 1..65535) { "GRPC_PORT must be a TCP port" } }
    }
}
