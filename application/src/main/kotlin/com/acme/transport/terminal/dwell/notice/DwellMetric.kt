package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell metric is in its life cycle. */
enum class DwellMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A dwell metric as the dwell team keeps it. */
data class DwellMetric(
    var code: String,
    var label: String,
    var pallets: Int,
    var discount: Double,
    var lane: String,
    var currency: String,
    var durationMinutes: Int,
    var phone: String,
    var status: DwellMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellMetricStatus): Boolean {
        when (status) {
            DwellMetricStatus.PROPOSED -> return next == DwellMetricStatus.REJECTED
            DwellMetricStatus.APPROVED -> {
                when (next) {
                    DwellMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            DwellMetricStatus.REJECTED -> return next == DwellMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DwellMetricStatus) {
        if (!canMoveTo(next)) {
            error("dwell metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dwell metric"} $code"

    fun effectiveValue(): Double = round((pallets.toDouble() + discount) * 100) / 100

    fun snapshot(): DwellMetric {
        return DwellMetric(
            code = code,
            label = label,
            pallets = pallets,
            discount = discount,
            lane = lane,
            currency = currency,
            durationMinutes = durationMinutes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dwell metric in status proposed. */
fun newDwellMetric(code: String, label: String): DwellMetric {
    return DwellMetric(
        code = code,
        label = label,
        pallets = 0,
        discount = 0.0,
        lane = "",
        currency = "",
        durationMinutes = 0,
        phone = "",
        status = DwellMetricStatus.PROPOSED,
    )
}
