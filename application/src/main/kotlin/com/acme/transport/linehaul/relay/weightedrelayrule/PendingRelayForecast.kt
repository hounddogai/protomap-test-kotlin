package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending relay forecast is in its life cycle. */
enum class PendingRelayForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending relay forecast. Its code is unique within a tenant. */
data class PendingRelayForecast(
    var code: String,
    var label: String,
    var cost: Double,
    var threshold: Double,
    var country: String,
    var retries: Int,
    var revision: Int,
    var channel: String,
    var floorValue: Double,
    var status: PendingRelayForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRelayForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRelayForecastStatus): Boolean {
        when (status) {
            PendingRelayForecastStatus.PLANNED -> {
                when (next) {
                    PendingRelayForecastStatus.IN_PROGRESS, PendingRelayForecastStatus.CANCELLED -> return true
                }
                return false
            }
            PendingRelayForecastStatus.SCHEDULED -> {
                when (next) {
                    PendingRelayForecastStatus.CANCELLED -> return true
                }
                return false
            }
            PendingRelayForecastStatus.IN_PROGRESS -> {
                when (next) {
                    PendingRelayForecastStatus.COMPLETED, PendingRelayForecastStatus.CANCELLED -> return true
                }
                return false
            }
            PendingRelayForecastStatus.COMPLETED -> return next == PendingRelayForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingRelayForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending relay forecast $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"pending relay forecast"} $code"

    fun adjustedValue(): Double = round((retries.toDouble() - floorValue) * 100) / 100

    fun snapshot(): PendingRelayForecast {
        return PendingRelayForecast(
            code = code,
            label = label,
            cost = cost,
            threshold = threshold,
            country = country,
            retries = retries,
            revision = revision,
            channel = channel,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending relay forecast in status planned. */
fun newPendingRelayForecast(code: String, label: String): PendingRelayForecast {
    return PendingRelayForecast(
        code = code,
        label = label,
        cost = 0.0,
        threshold = 0.0,
        country = "",
        retries = 0,
        revision = 0,
        channel = "",
        floorValue = 0.0,
        status = PendingRelayForecastStatus.PLANNED,
    )
}
