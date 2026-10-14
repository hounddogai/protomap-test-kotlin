package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation forecast is in its life cycle. */
enum class ViolationForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One violation forecast, with what the planners need to act on it. */
data class ViolationForecast(
    var code: String,
    var label: String,
    var quantity: Int,
    var pinned: Boolean,
    var depot: String,
    var country: String,
    var minimum: Double,
    var tags: MutableList<String>,
    var loadKg: Double,
    var channel: String,
    var status: ViolationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationForecastStatus): Boolean {
        when (status) {
            ViolationForecastStatus.REQUESTED -> {
                when (next) {
                    ViolationForecastStatus.GRANTED, ViolationForecastStatus.REVOKED -> return true
                }
                return false
            }
            ViolationForecastStatus.GRANTED -> {
                when (next) {
                    ViolationForecastStatus.EXPIRED -> return true
                }
                return false
            }
            ViolationForecastStatus.REVOKED -> return next == ViolationForecastStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ViolationForecastStatus) {
        if (!canMoveTo(next)) {
            error("violation forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"violation forecast"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(loadKg * quantity.toDouble() * 100) / 100

    fun snapshot(): ViolationForecast {
        return ViolationForecast(
            code = code,
            label = label,
            quantity = quantity,
            pinned = pinned,
            depot = depot,
            country = country,
            minimum = minimum,
            tags = tags,
            loadKg = loadKg,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a violation forecast in status requested. */
fun newViolationForecast(code: String, label: String): ViolationForecast {
    return ViolationForecast(
        code = code,
        label = label,
        quantity = 0,
        pinned = false,
        depot = "",
        country = "",
        minimum = 0.0,
        tags = mutableListOf<String>(),
        loadKg = 0.0,
        channel = "",
        status = ViolationForecastStatus.REQUESTED,
    )
}
