package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.text.normalize

/** Where a manual trailer metric is in its life cycle. */
enum class ManualTrailerMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One manual trailer metric, with what the planners need to act on it. */
data class ManualTrailerMetric(
    var code: String,
    var name: String,
    var graceDays: Int,
    var carrier: String,
    var timezone: String,
    var reviewer: String,
    var depot: String,
    var status: ManualTrailerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerMetricStatus): Boolean {
        when (status) {
            ManualTrailerMetricStatus.PLANNED -> {
                when (next) {
                    ManualTrailerMetricStatus.SCHEDULED, ManualTrailerMetricStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ManualTrailerMetricStatus.SCHEDULED -> return next == ManualTrailerMetricStatus.IN_PROGRESS || next == ManualTrailerMetricStatus.COMPLETED
            ManualTrailerMetricStatus.IN_PROGRESS -> {
                when (next) {
                    ManualTrailerMetricStatus.COMPLETED, ManualTrailerMetricStatus.CANCELLED -> return true
                }
                return false
            }
            ManualTrailerMetricStatus.COMPLETED -> {
                when (next) {
                    ManualTrailerMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerMetricStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): ManualTrailerMetric {
        return ManualTrailerMetric(
            code = code,
            name = name,
            graceDays = graceDays,
            carrier = carrier,
            timezone = timezone,
            reviewer = reviewer,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual trailer metric in status planned. */
fun newManualTrailerMetric(code: String, name: String): ManualTrailerMetric {
    return ManualTrailerMetric(
        code = code,
        name = name,
        graceDays = 0,
        carrier = "",
        timezone = "",
        reviewer = "",
        depot = "",
        status = ManualTrailerMetricStatus.PLANNED,
    )
}
