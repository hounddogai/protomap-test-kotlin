package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.text.normalize

/** Where a capacity forecast is in its life cycle. */
enum class CapacityForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One capacity forecast, with what the planners need to act on it. */
data class CapacityForecast(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var stopCount: Int,
    var category: String,
    var status: CapacityForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityForecastStatus): Boolean {
        when (status) {
            CapacityForecastStatus.PROPOSED -> return next == CapacityForecastStatus.REJECTED
            CapacityForecastStatus.APPROVED -> return next == CapacityForecastStatus.REJECTED
            CapacityForecastStatus.REJECTED -> {
                when (next) {
                    CapacityForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityForecastStatus) {
        if (!canMoveTo(next)) {
            error("capacity forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capacity forecast"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): CapacityForecast {
        return CapacityForecast(
            code = code,
            name = name,
            attributes = attributes,
            stopCount = stopCount,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capacity forecast in status proposed. */
fun newCapacityForecast(code: String, name: String): CapacityForecast {
    return CapacityForecast(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        stopCount = 0,
        category = "",
        status = CapacityForecastStatus.PROPOSED,
    )
}
