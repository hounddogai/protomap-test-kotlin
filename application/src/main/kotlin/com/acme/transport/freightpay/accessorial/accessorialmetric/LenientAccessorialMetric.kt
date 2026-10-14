package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.text.normalize

// The lenient accessorial metric records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a lenient accessorial metric is in its life cycle. */
enum class LenientAccessorialMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One lenient accessorial metric, with what the planners need to act on it. */
data class LenientAccessorialMetric(
    var code: String,
    var label: String,
    var unit: String,
    var stopCount: Int,
    var description: String,
    var lane: String,
    var status: LenientAccessorialMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAccessorialMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAccessorialMetricStatus): Boolean {
        when (status) {
            LenientAccessorialMetricStatus.OPEN -> {
                when (next) {
                    LenientAccessorialMetricStatus.RESOLVED, LenientAccessorialMetricStatus.REOPENED -> return true
                }
                return false
            }
            LenientAccessorialMetricStatus.IN_REVIEW -> return next == LenientAccessorialMetricStatus.REOPENED || next == LenientAccessorialMetricStatus.ARCHIVED
            LenientAccessorialMetricStatus.RESOLVED -> return next == LenientAccessorialMetricStatus.REOPENED || next == LenientAccessorialMetricStatus.ARCHIVED
            LenientAccessorialMetricStatus.REOPENED -> return next == LenientAccessorialMetricStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientAccessorialMetricStatus) {
        if (!canMoveTo(next)) {
            error("lenient accessorial metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): LenientAccessorialMetric {
        return LenientAccessorialMetric(
            code = code,
            label = label,
            unit = unit,
            stopCount = stopCount,
            description = description,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient accessorial metric in status open. */
fun newLenientAccessorialMetric(code: String, label: String): LenientAccessorialMetric {
    return LenientAccessorialMetric(
        code = code,
        label = label,
        unit = "",
        stopCount = 0,
        description = "",
        lane = "",
        status = LenientAccessorialMetricStatus.OPEN,
    )
}
