package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched service area exception is in its life cycle. */
enum class BatchedServiceAreaExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a batched service area exception and its current status. */
data class BatchedServiceAreaException(
    var code: String,
    var title: String,
    var segment: String,
    var locale: String,
    var ownerId: String,
    var margin: Double,
    var budget: Double,
    var score: Double,
    var retries: Int,
    var fuelLiters: Double,
    var status: BatchedServiceAreaExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServiceAreaExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServiceAreaExceptionStatus): Boolean {
        when (status) {
            BatchedServiceAreaExceptionStatus.DRAFT -> return next == BatchedServiceAreaExceptionStatus.ACTIVE
            BatchedServiceAreaExceptionStatus.PENDING -> {
                when (next) {
                    BatchedServiceAreaExceptionStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedServiceAreaExceptionStatus.ACTIVE -> return next == BatchedServiceAreaExceptionStatus.SUSPENDED || next == BatchedServiceAreaExceptionStatus.CLOSED
            BatchedServiceAreaExceptionStatus.SUSPENDED -> {
                when (next) {
                    BatchedServiceAreaExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedServiceAreaExceptionStatus) {
        if (!canMoveTo(next)) {
            error("batched service area exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round(retries.toDouble() * margin * 100) / 100

    fun snapshot(): BatchedServiceAreaException {
        return BatchedServiceAreaException(
            code = code,
            title = title,
            segment = segment,
            locale = locale,
            ownerId = ownerId,
            margin = margin,
            budget = budget,
            score = score,
            retries = retries,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched service area exception in status draft. */
fun newBatchedServiceAreaException(code: String, title: String): BatchedServiceAreaException {
    return BatchedServiceAreaException(
        code = code,
        title = title,
        segment = "",
        locale = "",
        ownerId = "",
        margin = 0.0,
        budget = 0.0,
        score = 0.0,
        retries = 0,
        fuelLiters = 0.0,
        status = BatchedServiceAreaExceptionStatus.DRAFT,
    )
}
