package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered roster exception is in its life cycle. */
enum class TieredRosterExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered roster exception, with what the planners need to act on it. */
data class TieredRosterException(
    var code: String,
    var title: String,
    var minimum: Double,
    var quantity: Int,
    var currency: String,
    var carrier: String,
    var cost: Double,
    var trailerId: String,
    var segment: String,
    var description: String,
    var status: TieredRosterExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRosterExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRosterExceptionStatus): Boolean {
        when (status) {
            TieredRosterExceptionStatus.PLANNED -> return next == TieredRosterExceptionStatus.CANCELLED
            TieredRosterExceptionStatus.SCHEDULED -> return next == TieredRosterExceptionStatus.IN_PROGRESS || next == TieredRosterExceptionStatus.COMPLETED
            TieredRosterExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    TieredRosterExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            TieredRosterExceptionStatus.COMPLETED -> return next == TieredRosterExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredRosterExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tiered roster exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered roster exception"} $code"

    fun netValue(): Double = round((cost - quantity.toDouble()) * 100) / 100

    fun snapshot(): TieredRosterException {
        return TieredRosterException(
            code = code,
            title = title,
            minimum = minimum,
            quantity = quantity,
            currency = currency,
            carrier = carrier,
            cost = cost,
            trailerId = trailerId,
            segment = segment,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered roster exception in status planned. */
fun newTieredRosterException(code: String, title: String): TieredRosterException {
    return TieredRosterException(
        code = code,
        title = title,
        minimum = 0.0,
        quantity = 0,
        currency = "",
        carrier = "",
        cost = 0.0,
        trailerId = "",
        segment = "",
        description = "",
        status = TieredRosterExceptionStatus.PLANNED,
    )
}
