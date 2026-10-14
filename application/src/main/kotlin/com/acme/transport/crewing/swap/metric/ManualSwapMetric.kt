package com.acme.transport.crewing.swap.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual swap metric records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a manual swap metric is in its life cycle. */
enum class ManualSwapMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual swap metric as the swap team keeps it. */
data class ManualSwapMetric(
    var code: String,
    var label: String,
    var summary: String,
    var depot: String,
    var unit: String,
    var budget: Double,
    var ceiling: Double,
    var lane: String,
    var capacity: Int,
    var status: ManualSwapMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualSwapMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualSwapMetricStatus): Boolean {
        when (status) {
            ManualSwapMetricStatus.NEW -> {
                when (next) {
                    ManualSwapMetricStatus.RUNNING, ManualSwapMetricStatus.FAILED -> return true
                }
                return false
            }
            ManualSwapMetricStatus.QUEUED -> return next == ManualSwapMetricStatus.RUNNING || next == ManualSwapMetricStatus.FAILED
            ManualSwapMetricStatus.RUNNING -> return next == ManualSwapMetricStatus.FAILED
            ManualSwapMetricStatus.DONE -> {
                when (next) {
                    ManualSwapMetricStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualSwapMetricStatus) {
        if (!canMoveTo(next)) {
            error("manual swap metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round(budget * ceiling * 100) / 100

    fun snapshot(): ManualSwapMetric {
        return ManualSwapMetric(
            code = code,
            label = label,
            summary = summary,
            depot = depot,
            unit = unit,
            budget = budget,
            ceiling = ceiling,
            lane = lane,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual swap metric in status new. */
fun newManualSwapMetric(code: String, label: String): ManualSwapMetric {
    return ManualSwapMetric(
        code = code,
        label = label,
        summary = "",
        depot = "",
        unit = "",
        budget = 0.0,
        ceiling = 0.0,
        lane = "",
        capacity = 0,
        status = ManualSwapMetricStatus.NEW,
    )
}
