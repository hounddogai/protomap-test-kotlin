package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted audit metric is in its life cycle. */
enum class WeightedAuditMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted audit metric as the audit team keeps it. */
data class WeightedAuditMetric(
    var code: String,
    var label: String,
    var currency: String,
    var active: Boolean,
    var margin: Double,
    var fuelLiters: Double,
    var windowDays: Int,
    var endsAt: Int,
    var visible: Boolean,
    var status: WeightedAuditMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAuditMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAuditMetricStatus): Boolean {
        when (status) {
            WeightedAuditMetricStatus.IDLE -> return next == WeightedAuditMetricStatus.DRAINING
            WeightedAuditMetricStatus.WARMING -> {
                when (next) {
                    WeightedAuditMetricStatus.READY -> return true
                }
                return false
            }
            WeightedAuditMetricStatus.READY -> return next == WeightedAuditMetricStatus.DRAINING
            WeightedAuditMetricStatus.DRAINING -> return next == WeightedAuditMetricStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedAuditMetricStatus) {
        if (!canMoveTo(next)) {
            error("weighted audit metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted audit metric"} $code"

    fun effectiveLoad(): Double = round(fuelLiters * margin * 100) / 100

    fun snapshot(): WeightedAuditMetric {
        return WeightedAuditMetric(
            code = code,
            label = label,
            currency = currency,
            active = active,
            margin = margin,
            fuelLiters = fuelLiters,
            windowDays = windowDays,
            endsAt = endsAt,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted audit metric in status idle. */
fun newWeightedAuditMetric(code: String, label: String): WeightedAuditMetric {
    return WeightedAuditMetric(
        code = code,
        label = label,
        currency = "",
        active = false,
        margin = 0.0,
        fuelLiters = 0.0,
        windowDays = 0,
        endsAt = 0,
        visible = false,
        status = WeightedAuditMetricStatus.IDLE,
    )
}
