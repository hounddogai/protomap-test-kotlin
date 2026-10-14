package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.kit.text.normalize

/** Where a trailer yard metric is in its life cycle. */
enum class TrailerYardMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A trailer yard metric as the trailer yard team keeps it. */
data class TrailerYardMetric(
    var code: String,
    var title: String,
    var category: String,
    var floorValue: Double,
    var approved: Boolean,
    var lane: String,
    var segment: String,
    var status: TrailerYardMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardMetricStatus): Boolean {
        when (status) {
            TrailerYardMetricStatus.PLANNED -> {
                when (next) {
                    TrailerYardMetricStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerYardMetricStatus.SCHEDULED -> return next == TrailerYardMetricStatus.CANCELLED
            TrailerYardMetricStatus.IN_PROGRESS -> return next == TrailerYardMetricStatus.COMPLETED
            TrailerYardMetricStatus.COMPLETED -> return next == TrailerYardMetricStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TrailerYardMetricStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = floorValue

    fun snapshot(): TrailerYardMetric {
        return TrailerYardMetric(
            code = code,
            title = title,
            category = category,
            floorValue = floorValue,
            approved = approved,
            lane = lane,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a trailer yard metric in status planned. */
fun newTrailerYardMetric(code: String, title: String): TrailerYardMetric {
    return TrailerYardMetric(
        code = code,
        title = title,
        category = "",
        floorValue = 0.0,
        approved = false,
        lane = "",
        segment = "",
        status = TrailerYardMetricStatus.PLANNED,
    )
}
