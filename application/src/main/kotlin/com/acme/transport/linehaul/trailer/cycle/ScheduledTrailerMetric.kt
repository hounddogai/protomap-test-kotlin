package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled trailer metric is in its life cycle. */
enum class ScheduledTrailerMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled trailer metric, with what the planners need to act on it. */
data class ScheduledTrailerMetric(
    var code: String,
    var title: String,
    var source: String,
    var discount: Double,
    var priority: Int,
    var name: String,
    var status: ScheduledTrailerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerMetricStatus): Boolean {
        when (status) {
            ScheduledTrailerMetricStatus.NEW -> {
                when (next) {
                    ScheduledTrailerMetricStatus.QUEUED, ScheduledTrailerMetricStatus.DONE -> return true
                }
                return false
            }
            ScheduledTrailerMetricStatus.QUEUED -> {
                when (next) {
                    ScheduledTrailerMetricStatus.RUNNING, ScheduledTrailerMetricStatus.DONE -> return true
                }
                return false
            }
            ScheduledTrailerMetricStatus.RUNNING -> return next == ScheduledTrailerMetricStatus.DONE || next == ScheduledTrailerMetricStatus.FAILED
            ScheduledTrailerMetricStatus.DONE -> {
                when (next) {
                    ScheduledTrailerMetricStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerMetricStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round((priority.toDouble() + discount) * 100) / 100

    fun snapshot(): ScheduledTrailerMetric {
        return ScheduledTrailerMetric(
            code = code,
            title = title,
            source = source,
            discount = discount,
            priority = priority,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled trailer metric in status new. */
fun newScheduledTrailerMetric(code: String, title: String): ScheduledTrailerMetric {
    return ScheduledTrailerMetric(
        code = code,
        title = title,
        source = "",
        discount = 0.0,
        priority = 0,
        name = "",
        status = ScheduledTrailerMetricStatus.NEW,
    )
}
