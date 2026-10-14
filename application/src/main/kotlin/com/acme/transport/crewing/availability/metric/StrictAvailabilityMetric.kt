package com.acme.transport.crewing.availability.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict availability metric records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a strict availability metric is in its life cycle. */
enum class StrictAvailabilityMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One strict availability metric, with what the planners need to act on it. */
data class StrictAvailabilityMetric(
    var code: String,
    var name: String,
    var summary: String,
    var pallets: Int,
    var tags: MutableList<String>,
    var cost: Double,
    var status: StrictAvailabilityMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAvailabilityMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAvailabilityMetricStatus): Boolean {
        when (status) {
            StrictAvailabilityMetricStatus.CREATED -> return next == StrictAvailabilityMetricStatus.RETIRED
            StrictAvailabilityMetricStatus.VALIDATED -> return next == StrictAvailabilityMetricStatus.PUBLISHED || next == StrictAvailabilityMetricStatus.RETIRED
            StrictAvailabilityMetricStatus.PUBLISHED -> return next == StrictAvailabilityMetricStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictAvailabilityMetricStatus) {
        if (!canMoveTo(next)) {
            error("strict availability metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict availability metric"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((pallets.toDouble() + cost) * 100) / 100

    fun snapshot(): StrictAvailabilityMetric {
        return StrictAvailabilityMetric(
            code = code,
            name = name,
            summary = summary,
            pallets = pallets,
            tags = tags,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict availability metric in status created. */
fun newStrictAvailabilityMetric(code: String, name: String): StrictAvailabilityMetric {
    return StrictAvailabilityMetric(
        code = code,
        name = name,
        summary = "",
        pallets = 0,
        tags = mutableListOf<String>(),
        cost = 0.0,
        status = StrictAvailabilityMetricStatus.CREATED,
    )
}
