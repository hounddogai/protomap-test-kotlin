package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.normalize

/** Where a broker forecast is in its life cycle. */
enum class BrokerForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A broker forecast. Its code is unique within a tenant. */
data class BrokerForecast(
    var code: String,
    var name: String,
    var label: String,
    var startsAt: Int,
    var driverId: String,
    var status: BrokerForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerForecastStatus): Boolean {
        when (status) {
            BrokerForecastStatus.PLANNED -> return next == BrokerForecastStatus.SCHEDULED || next == BrokerForecastStatus.CANCELLED
            BrokerForecastStatus.SCHEDULED -> return next == BrokerForecastStatus.CANCELLED
            BrokerForecastStatus.IN_PROGRESS -> return next == BrokerForecastStatus.CANCELLED
            BrokerForecastStatus.COMPLETED -> {
                when (next) {
                    BrokerForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerForecastStatus) {
        if (!canMoveTo(next)) {
            error("broker forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): BrokerForecast {
        return BrokerForecast(
            code = code,
            name = name,
            label = label,
            startsAt = startsAt,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a broker forecast in status planned. */
fun newBrokerForecast(code: String, name: String): BrokerForecast {
    return BrokerForecast(
        code = code,
        name = name,
        label = "",
        startsAt = 0,
        driverId = "",
        status = BrokerForecastStatus.PLANNED,
    )
}
