package com.acme.transport.platform

import com.acme.common.v1.RequestContext
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import java.sql.SQLException

/** Translate application failures once, keeping gRPC statuses and coroutine cancellation intact. */
object RpcBoundary {
    private val log = LoggerFactory.getLogger(RpcBoundary::class.java)

    fun validate(context: RequestContext) {
        if (context.tenantId.isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("context.tenant_id is required").asRuntimeException()
        }
    }

    suspend fun <T> run(operation: String, body: suspend () -> T): T = try {
        withTimeout(10_000) { body() }
    } catch (error: TimeoutCancellationException) {
        throw Status.DEADLINE_EXCEEDED.withDescription(operation).withCause(error).asRuntimeException()
    } catch (error: CancellationException) {
        throw error
    } catch (error: StatusException) {
        throw error
    } catch (error: StatusRuntimeException) {
        throw error
    } catch (error: IllegalArgumentException) {
        throw Status.INVALID_ARGUMENT.withDescription(error.message).withCause(error).asRuntimeException()
    } catch (error: SQLException) {
        log.atError().addKeyValue("operation", operation).setCause(error).log("Database operation failed")
        throw Status.UNAVAILABLE.withDescription("database unavailable").withCause(error).asRuntimeException()
    } catch (error: Exception) {
        log.atError().addKeyValue("operation", operation).setCause(error).log("Request failed")
        throw Status.INTERNAL.withDescription(operation).withCause(error).asRuntimeException()
    }

    /** Only callers of read operations use retries; mutations retain their original idempotency semantics. */
    suspend fun <T> read(body: suspend () -> T): T {
        repeat(2) { attempt ->
            try {
                return withTimeout(5_000) { body() }
            } catch (error: StatusException) {
                if (error.status.code != Status.Code.UNAVAILABLE) throw error
            } catch (error: StatusRuntimeException) {
                if (error.status.code != Status.Code.UNAVAILABLE) throw error
            }
            delay(25L * (attempt + 1))
        }
        return withTimeout(5_000) { body() }
    }
}
