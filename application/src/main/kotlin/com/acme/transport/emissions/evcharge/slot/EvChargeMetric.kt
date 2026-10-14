package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.text.normalize

/** Where a ev charge metric is in its life cycle. */
enum class EvChargeMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A ev charge metric. Its code is unique within a tenant. */
data class EvChargeMetric(
    var code: String,
    var label: String,
    var loadKg: Double,
    var channel: String,
    var approved: Boolean,
    var status: EvChargeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeMetricStatus): Boolean {
        when (status) {
            EvChargeMetricStatus.PROPOSED -> return next == EvChargeMetricStatus.REJECTED || next == EvChargeMetricStatus.WITHDRAWN
            EvChargeMetricStatus.APPROVED -> return next == EvChargeMetricStatus.REJECTED
            EvChargeMetricStatus.REJECTED -> {
                when (next) {
                    EvChargeMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeMetricStatus) {
        if (!canMoveTo(next)) {
            error("ev charge metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = loadKg

    fun snapshot(): EvChargeMetric {
        return EvChargeMetric(
            code = code,
            label = label,
            loadKg = loadKg,
            channel = channel,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a ev charge metric in status proposed. */
fun newEvChargeMetric(code: String, label: String): EvChargeMetric {
    return EvChargeMetric(
        code = code,
        label = label,
        loadKg = 0.0,
        channel = "",
        approved = false,
        status = EvChargeMetricStatus.PROPOSED,
    )
}
