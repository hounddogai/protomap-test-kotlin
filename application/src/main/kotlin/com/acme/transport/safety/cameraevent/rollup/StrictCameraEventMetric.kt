package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict camera event metric is in its life cycle. */
enum class StrictCameraEventMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One strict camera event metric, with what the planners need to act on it. */
data class StrictCameraEventMetric(
    var code: String,
    var title: String,
    var currency: String,
    var minimum: Double,
    var source: String,
    var country: String,
    var version: Int,
    var durationMinutes: Int,
    var status: StrictCameraEventMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCameraEventMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCameraEventMetricStatus): Boolean {
        when (status) {
            StrictCameraEventMetricStatus.IDLE -> return next == StrictCameraEventMetricStatus.READY
            StrictCameraEventMetricStatus.WARMING -> {
                when (next) {
                    StrictCameraEventMetricStatus.STOPPED -> return true
                }
                return false
            }
            StrictCameraEventMetricStatus.READY -> return next == StrictCameraEventMetricStatus.STOPPED
            StrictCameraEventMetricStatus.DRAINING -> {
                when (next) {
                    StrictCameraEventMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCameraEventMetricStatus) {
        if (!canMoveTo(next)) {
            error("strict camera event metric $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"strict camera event metric"} $code"

    fun effectiveLoad(): Double = round((durationMinutes.toDouble() - minimum) * 100) / 100

    fun snapshot(): StrictCameraEventMetric {
        return StrictCameraEventMetric(
            code = code,
            title = title,
            currency = currency,
            minimum = minimum,
            source = source,
            country = country,
            version = version,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict camera event metric in status idle. */
fun newStrictCameraEventMetric(code: String, title: String): StrictCameraEventMetric {
    return StrictCameraEventMetric(
        code = code,
        title = title,
        currency = "",
        minimum = 0.0,
        source = "",
        country = "",
        version = 0,
        durationMinutes = 0,
        status = StrictCameraEventMetricStatus.IDLE,
    )
}
