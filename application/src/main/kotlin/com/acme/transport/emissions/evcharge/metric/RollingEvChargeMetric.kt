package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling ev charge metric records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a rolling ev charge metric is in its life cycle. */
enum class RollingEvChargeMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling ev charge metric as the ev charge team keeps it. */
data class RollingEvChargeMetric(
    var code: String,
    var label: String,
    var quantity: Int,
    var checksum: String,
    var loadKg: Double,
    var pallets: Int,
    var budget: Double,
    var currency: String,
    var status: RollingEvChargeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingEvChargeMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingEvChargeMetricStatus): Boolean {
        when (status) {
            RollingEvChargeMetricStatus.NEW -> {
                when (next) {
                    RollingEvChargeMetricStatus.QUEUED, RollingEvChargeMetricStatus.RUNNING -> return true
                }
                return false
            }
            RollingEvChargeMetricStatus.QUEUED -> return next == RollingEvChargeMetricStatus.RUNNING || next == RollingEvChargeMetricStatus.FAILED
            RollingEvChargeMetricStatus.RUNNING -> return next == RollingEvChargeMetricStatus.DONE || next == RollingEvChargeMetricStatus.FAILED
            RollingEvChargeMetricStatus.DONE -> return next == RollingEvChargeMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingEvChargeMetricStatus) {
        if (!canMoveTo(next)) {
            error("rolling ev charge metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling ev charge metric"} $code"

    fun effectiveVolume(): Double = round((pallets.toDouble() + loadKg) * 100) / 100

    fun snapshot(): RollingEvChargeMetric {
        return RollingEvChargeMetric(
            code = code,
            label = label,
            quantity = quantity,
            checksum = checksum,
            loadKg = loadKg,
            pallets = pallets,
            budget = budget,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling ev charge metric in status new. */
fun newRollingEvChargeMetric(code: String, label: String): RollingEvChargeMetric {
    return RollingEvChargeMetric(
        code = code,
        label = label,
        quantity = 0,
        checksum = "",
        loadKg = 0.0,
        pallets = 0,
        budget = 0.0,
        currency = "",
        status = RollingEvChargeMetricStatus.NEW,
    )
}
