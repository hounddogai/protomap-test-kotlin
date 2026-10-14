package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.kit.text.normalize

// The weighted utilization segment records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a weighted utilization segment is in its life cycle. */
enum class WeightedUtilizationSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted utilization segment as the utilization team keeps it. */
data class WeightedUtilizationSegment(
    var code: String,
    var label: String,
    var active: Boolean,
    var externalRef: String,
    var segment: String,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var status: WeightedUtilizationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedUtilizationSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedUtilizationSegmentStatus): Boolean {
        when (status) {
            WeightedUtilizationSegmentStatus.CREATED -> {
                when (next) {
                    WeightedUtilizationSegmentStatus.RETIRED -> return true
                }
                return false
            }
            WeightedUtilizationSegmentStatus.VALIDATED -> {
                when (next) {
                    WeightedUtilizationSegmentStatus.PUBLISHED, WeightedUtilizationSegmentStatus.RETIRED -> return true
                }
                return false
            }
            WeightedUtilizationSegmentStatus.PUBLISHED -> return next == WeightedUtilizationSegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedUtilizationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted utilization segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted utilization segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = ceiling

    fun snapshot(): WeightedUtilizationSegment {
        return WeightedUtilizationSegment(
            code = code,
            label = label,
            active = active,
            externalRef = externalRef,
            segment = segment,
            dock = dock,
            attributes = attributes,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted utilization segment in status created. */
fun newWeightedUtilizationSegment(code: String, label: String): WeightedUtilizationSegment {
    return WeightedUtilizationSegment(
        code = code,
        label = label,
        active = false,
        externalRef = "",
        segment = "",
        dock = "",
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        status = WeightedUtilizationSegmentStatus.CREATED,
    )
}
