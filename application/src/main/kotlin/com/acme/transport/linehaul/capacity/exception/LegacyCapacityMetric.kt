package com.acme.transport.linehaul.capacity.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy capacity metric is in its life cycle. */
enum class LegacyCapacityMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy capacity metric. Its code is unique within a tenant. */
data class LegacyCapacityMetric(
    var code: String,
    var label: String,
    var retries: Int,
    var maximum: Double,
    var pinned: Boolean,
    var threshold: Double,
    var phone: String,
    var floorValue: Double,
    var status: LegacyCapacityMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCapacityMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCapacityMetricStatus): Boolean {
        when (status) {
            LegacyCapacityMetricStatus.NEW -> {
                when (next) {
                    LegacyCapacityMetricStatus.DONE -> return true
                }
                return false
            }
            LegacyCapacityMetricStatus.QUEUED -> return next == LegacyCapacityMetricStatus.RUNNING
            LegacyCapacityMetricStatus.RUNNING -> return next == LegacyCapacityMetricStatus.DONE || next == LegacyCapacityMetricStatus.FAILED
            LegacyCapacityMetricStatus.DONE -> {
                when (next) {
                    LegacyCapacityMetricStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCapacityMetricStatus) {
        if (!canMoveTo(next)) {
            error("legacy capacity metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy capacity metric"} $code"

    fun projectedVolume(): Double = round(floorValue * retries.toDouble() * 100) / 100

    fun snapshot(): LegacyCapacityMetric {
        return LegacyCapacityMetric(
            code = code,
            label = label,
            retries = retries,
            maximum = maximum,
            pinned = pinned,
            threshold = threshold,
            phone = phone,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy capacity metric in status new. */
fun newLegacyCapacityMetric(code: String, label: String): LegacyCapacityMetric {
    return LegacyCapacityMetric(
        code = code,
        label = label,
        retries = 0,
        maximum = 0.0,
        pinned = false,
        threshold = 0.0,
        phone = "",
        floorValue = 0.0,
        status = LegacyCapacityMetricStatus.NEW,
    )
}
