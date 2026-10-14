package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.text.normalize

/** Where a regional ev charge metric is in its life cycle. */
enum class RegionalEvChargeMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional ev charge metric and its current status. */
data class RegionalEvChargeMetric(
    var code: String,
    var title: String,
    var description: String,
    var vehicleId: String,
    var loadKg: Double,
    var status: RegionalEvChargeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalEvChargeMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalEvChargeMetricStatus): Boolean {
        when (status) {
            RegionalEvChargeMetricStatus.IDLE -> return next == RegionalEvChargeMetricStatus.WARMING || next == RegionalEvChargeMetricStatus.DRAINING
            RegionalEvChargeMetricStatus.WARMING -> {
                when (next) {
                    RegionalEvChargeMetricStatus.READY, RegionalEvChargeMetricStatus.DRAINING -> return true
                }
                return false
            }
            RegionalEvChargeMetricStatus.READY -> return next == RegionalEvChargeMetricStatus.DRAINING || next == RegionalEvChargeMetricStatus.STOPPED
            RegionalEvChargeMetricStatus.DRAINING -> return next == RegionalEvChargeMetricStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalEvChargeMetricStatus) {
        if (!canMoveTo(next)) {
            error("regional ev charge metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = loadKg

    fun snapshot(): RegionalEvChargeMetric {
        return RegionalEvChargeMetric(
            code = code,
            title = title,
            description = description,
            vehicleId = vehicleId,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional ev charge metric in status idle. */
fun newRegionalEvChargeMetric(code: String, title: String): RegionalEvChargeMetric {
    return RegionalEvChargeMetric(
        code = code,
        title = title,
        description = "",
        vehicleId = "",
        loadKg = 0.0,
        status = RegionalEvChargeMetricStatus.IDLE,
    )
}
