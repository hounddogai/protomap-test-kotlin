package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery exception records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery exception is in its life cycle. */
enum class RedeliveryExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One redelivery exception, with what the planners need to act on it. */
data class RedeliveryException(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var approved: Boolean,
    var odometer: Int,
    var summary: String,
    var currency: String,
    var externalRef: String,
    var distanceKm: Double,
    var status: RedeliveryExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryExceptionStatus): Boolean {
        when (status) {
            RedeliveryExceptionStatus.DRAFT -> {
                when (next) {
                    RedeliveryExceptionStatus.ACTIVE, RedeliveryExceptionStatus.SUSPENDED -> return true
                }
                return false
            }
            RedeliveryExceptionStatus.PENDING -> return next == RedeliveryExceptionStatus.SUSPENDED || next == RedeliveryExceptionStatus.CLOSED
            RedeliveryExceptionStatus.ACTIVE -> return next == RedeliveryExceptionStatus.SUSPENDED || next == RedeliveryExceptionStatus.CLOSED
            RedeliveryExceptionStatus.SUSPENDED -> return next == RedeliveryExceptionStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RedeliveryExceptionStatus) {
        if (!canMoveTo(next)) {
            error("redelivery exception $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((odometer.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryException {
        return RedeliveryException(
            code = code,
            title = title,
            updatedAt = updatedAt,
            approved = approved,
            odometer = odometer,
            summary = summary,
            currency = currency,
            externalRef = externalRef,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a redelivery exception in status draft. */
fun newRedeliveryException(code: String, title: String): RedeliveryException {
    return RedeliveryException(
        code = code,
        title = title,
        updatedAt = 0,
        approved = false,
        odometer = 0,
        summary = "",
        currency = "",
        externalRef = "",
        distanceKm = 0.0,
        status = RedeliveryExceptionStatus.DRAFT,
    )
}
