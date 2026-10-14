package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.normalize

// The regional broker metric records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a regional broker metric is in its life cycle. */
enum class RegionalBrokerMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional broker metric as the broker team keeps it. */
data class RegionalBrokerMetric(
    var code: String,
    var name: String,
    var notes: String,
    var category: String,
    var segment: String,
    var driverId: String,
    var shiftCode: String,
    var minimum: Double,
    var carrier: String,
    var status: RegionalBrokerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBrokerMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBrokerMetricStatus): Boolean {
        when (status) {
            RegionalBrokerMetricStatus.CREATED -> {
                when (next) {
                    RegionalBrokerMetricStatus.PUBLISHED, RegionalBrokerMetricStatus.RETIRED -> return true
                }
                return false
            }
            RegionalBrokerMetricStatus.VALIDATED -> return next == RegionalBrokerMetricStatus.PUBLISHED
            RegionalBrokerMetricStatus.PUBLISHED -> {
                when (next) {
                    RegionalBrokerMetricStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalBrokerMetricStatus) {
        if (!canMoveTo(next)) {
            error("regional broker metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional broker metric"} $code"

    fun measuredValue(): Double = minimum

    fun snapshot(): RegionalBrokerMetric {
        return RegionalBrokerMetric(
            code = code,
            name = name,
            notes = notes,
            category = category,
            segment = segment,
            driverId = driverId,
            shiftCode = shiftCode,
            minimum = minimum,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional broker metric in status created. */
fun newRegionalBrokerMetric(code: String, name: String): RegionalBrokerMetric {
    return RegionalBrokerMetric(
        code = code,
        name = name,
        notes = "",
        category = "",
        segment = "",
        driverId = "",
        shiftCode = "",
        minimum = 0.0,
        carrier = "",
        status = RegionalBrokerMetricStatus.CREATED,
    )
}
