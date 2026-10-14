package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending availability forecast is in its life cycle. */
enum class PendingAvailabilityForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending availability forecast as the availability team keeps it. */
data class PendingAvailabilityForecast(
    var code: String,
    var name: String,
    var endsAt: Int,
    var channel: String,
    var updatedAt: Int,
    var graceDays: Int,
    var ceiling: Double,
    var createdAt: Int,
    var margin: Double,
    var status: PendingAvailabilityForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAvailabilityForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAvailabilityForecastStatus): Boolean {
        when (status) {
            PendingAvailabilityForecastStatus.PROPOSED -> return next == PendingAvailabilityForecastStatus.REJECTED || next == PendingAvailabilityForecastStatus.WITHDRAWN
            PendingAvailabilityForecastStatus.APPROVED -> {
                when (next) {
                    PendingAvailabilityForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingAvailabilityForecastStatus.REJECTED -> return next == PendingAvailabilityForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingAvailabilityForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending availability forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((ceiling - graceDays.toDouble()) * 100) / 100

    fun snapshot(): PendingAvailabilityForecast {
        return PendingAvailabilityForecast(
            code = code,
            name = name,
            endsAt = endsAt,
            channel = channel,
            updatedAt = updatedAt,
            graceDays = graceDays,
            ceiling = ceiling,
            createdAt = createdAt,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending availability forecast in status proposed. */
fun newPendingAvailabilityForecast(code: String, name: String): PendingAvailabilityForecast {
    return PendingAvailabilityForecast(
        code = code,
        name = name,
        endsAt = 0,
        channel = "",
        updatedAt = 0,
        graceDays = 0,
        ceiling = 0.0,
        createdAt = 0,
        margin = 0.0,
        status = PendingAvailabilityForecastStatus.PROPOSED,
    )
}
