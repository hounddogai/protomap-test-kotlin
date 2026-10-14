package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary fuel burn metric records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a primary fuel burn metric is in its life cycle. */
enum class PrimaryFuelBurnMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary fuel burn metric as the fuel burn team keeps it. */
data class PrimaryFuelBurnMetric(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var graceDays: Int,
    var endsAt: Int,
    var unit: String,
    var ratio: Double,
    var maximum: Double,
    var channel: String,
    var ownerId: String,
    var phone: String,
    var status: PrimaryFuelBurnMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFuelBurnMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFuelBurnMetricStatus): Boolean {
        when (status) {
            PrimaryFuelBurnMetricStatus.CREATED -> return next == PrimaryFuelBurnMetricStatus.VALIDATED || next == PrimaryFuelBurnMetricStatus.RETIRED
            PrimaryFuelBurnMetricStatus.VALIDATED -> {
                when (next) {
                    PrimaryFuelBurnMetricStatus.PUBLISHED, PrimaryFuelBurnMetricStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryFuelBurnMetricStatus.PUBLISHED -> return next == PrimaryFuelBurnMetricStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryFuelBurnMetricStatus) {
        if (!canMoveTo(next)) {
            error("primary fuel burn metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveExposure(): Double = round(endsAt.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): PrimaryFuelBurnMetric {
        return PrimaryFuelBurnMetric(
            code = code,
            label = label,
            updatedAt = updatedAt,
            graceDays = graceDays,
            endsAt = endsAt,
            unit = unit,
            ratio = ratio,
            maximum = maximum,
            channel = channel,
            ownerId = ownerId,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary fuel burn metric in status created. */
fun newPrimaryFuelBurnMetric(code: String, label: String): PrimaryFuelBurnMetric {
    return PrimaryFuelBurnMetric(
        code = code,
        label = label,
        updatedAt = 0,
        graceDays = 0,
        endsAt = 0,
        unit = "",
        ratio = 0.0,
        maximum = 0.0,
        channel = "",
        ownerId = "",
        phone = "",
        status = PrimaryFuelBurnMetricStatus.CREATED,
    )
}
