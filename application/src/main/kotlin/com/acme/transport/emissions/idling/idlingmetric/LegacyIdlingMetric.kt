package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy idling metric records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a legacy idling metric is in its life cycle. */
enum class LegacyIdlingMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy idling metric as the idling team keeps it. */
data class LegacyIdlingMetric(
    var code: String,
    var label: String,
    var description: String,
    var threshold: Double,
    var batchSize: Int,
    var status: LegacyIdlingMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIdlingMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIdlingMetricStatus): Boolean {
        when (status) {
            LegacyIdlingMetricStatus.NEW -> {
                when (next) {
                    LegacyIdlingMetricStatus.QUEUED, LegacyIdlingMetricStatus.FAILED -> return true
                }
                return false
            }
            LegacyIdlingMetricStatus.QUEUED -> return next == LegacyIdlingMetricStatus.DONE || next == LegacyIdlingMetricStatus.FAILED
            LegacyIdlingMetricStatus.RUNNING -> return next == LegacyIdlingMetricStatus.FAILED
            LegacyIdlingMetricStatus.DONE -> {
                when (next) {
                    LegacyIdlingMetricStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyIdlingMetricStatus) {
        if (!canMoveTo(next)) {
            error("legacy idling metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((threshold + batchSize.toDouble()) * 100) / 100

    fun snapshot(): LegacyIdlingMetric {
        return LegacyIdlingMetric(
            code = code,
            label = label,
            description = description,
            threshold = threshold,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy idling metric in status new. */
fun newLegacyIdlingMetric(code: String, label: String): LegacyIdlingMetric {
    return LegacyIdlingMetric(
        code = code,
        label = label,
        description = "",
        threshold = 0.0,
        batchSize = 0,
        status = LegacyIdlingMetricStatus.NEW,
    )
}
