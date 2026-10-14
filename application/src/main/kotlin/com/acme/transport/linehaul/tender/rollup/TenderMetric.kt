package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.normalize

/** Where a tender metric is in its life cycle. */
enum class TenderMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tender metric and its current status. */
data class TenderMetric(
    var code: String,
    var label: String,
    var depot: String,
    var updatedAt: Int,
    var reviewer: String,
    var active: Boolean,
    var status: TenderMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderMetricStatus): Boolean {
        when (status) {
            TenderMetricStatus.OPEN -> return next == TenderMetricStatus.RESOLVED || next == TenderMetricStatus.REOPENED
            TenderMetricStatus.IN_REVIEW -> return next == TenderMetricStatus.REOPENED
            TenderMetricStatus.RESOLVED -> {
                when (next) {
                    TenderMetricStatus.REOPENED, TenderMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            TenderMetricStatus.REOPENED -> {
                when (next) {
                    TenderMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderMetricStatus) {
        if (!canMoveTo(next)) {
            error("tender metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): TenderMetric {
        return TenderMetric(
            code = code,
            label = label,
            depot = depot,
            updatedAt = updatedAt,
            reviewer = reviewer,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tender metric in status open. */
fun newTenderMetric(code: String, label: String): TenderMetric {
    return TenderMetric(
        code = code,
        label = label,
        depot = "",
        updatedAt = 0,
        reviewer = "",
        active = false,
        status = TenderMetricStatus.OPEN,
    )
}
