package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled defect metric is in its life cycle. */
enum class ScheduledDefectMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One scheduled defect metric, with what the planners need to act on it. */
data class ScheduledDefectMetric(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var attempts: Int,
    var fuelLiters: Double,
    var status: ScheduledDefectMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDefectMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDefectMetricStatus): Boolean {
        when (status) {
            ScheduledDefectMetricStatus.OPEN -> return next == ScheduledDefectMetricStatus.ARCHIVED
            ScheduledDefectMetricStatus.IN_REVIEW -> return next == ScheduledDefectMetricStatus.RESOLVED
            ScheduledDefectMetricStatus.RESOLVED -> {
                when (next) {
                    ScheduledDefectMetricStatus.REOPENED, ScheduledDefectMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledDefectMetricStatus.REOPENED -> return next == ScheduledDefectMetricStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledDefectMetricStatus) {
        if (!canMoveTo(next)) {
            error("scheduled defect metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveValue(): Double = round((attempts.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDefectMetric {
        return ScheduledDefectMetric(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            attempts = attempts,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled defect metric in status open. */
fun newScheduledDefectMetric(code: String, title: String): ScheduledDefectMetric {
    return ScheduledDefectMetric(
        code = code,
        title = title,
        etaMinutes = 0,
        attempts = 0,
        fuelLiters = 0.0,
        status = ScheduledDefectMetricStatus.OPEN,
    )
}
