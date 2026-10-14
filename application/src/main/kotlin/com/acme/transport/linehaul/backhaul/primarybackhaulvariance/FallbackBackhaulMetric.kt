package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback backhaul metric is in its life cycle. */
enum class FallbackBackhaulMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback backhaul metric. Its code is unique within a tenant. */
data class FallbackBackhaulMetric(
    var code: String,
    var label: String,
    var carrier: String,
    var ceiling: Double,
    var weight: Double,
    var amount: Double,
    var unit: String,
    var driverId: String,
    var status: FallbackBackhaulMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBackhaulMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBackhaulMetricStatus): Boolean {
        when (status) {
            FallbackBackhaulMetricStatus.PROPOSED -> return next == FallbackBackhaulMetricStatus.APPROVED
            FallbackBackhaulMetricStatus.APPROVED -> {
                when (next) {
                    FallbackBackhaulMetricStatus.REJECTED, FallbackBackhaulMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackBackhaulMetricStatus.REJECTED -> {
                when (next) {
                    FallbackBackhaulMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackBackhaulMetricStatus) {
        if (!canMoveTo(next)) {
            error("fallback backhaul metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round((ceiling - amount) * 100) / 100

    fun snapshot(): FallbackBackhaulMetric {
        return FallbackBackhaulMetric(
            code = code,
            label = label,
            carrier = carrier,
            ceiling = ceiling,
            weight = weight,
            amount = amount,
            unit = unit,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback backhaul metric in status proposed. */
fun newFallbackBackhaulMetric(code: String, label: String): FallbackBackhaulMetric {
    return FallbackBackhaulMetric(
        code = code,
        label = label,
        carrier = "",
        ceiling = 0.0,
        weight = 0.0,
        amount = 0.0,
        unit = "",
        driverId = "",
        status = FallbackBackhaulMetricStatus.PROPOSED,
    )
}
