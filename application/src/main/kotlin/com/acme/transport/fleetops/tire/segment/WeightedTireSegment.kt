package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted tire segment records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a weighted tire segment is in its life cycle. */
enum class WeightedTireSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted tire segment as the tire team keeps it. */
data class WeightedTireSegment(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var odometer: Int,
    var tags: MutableList<String>,
    var summary: String,
    var status: WeightedTireSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTireSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTireSegmentStatus): Boolean {
        when (status) {
            WeightedTireSegmentStatus.CREATED -> {
                when (next) {
                    WeightedTireSegmentStatus.RETIRED -> return true
                }
                return false
            }
            WeightedTireSegmentStatus.VALIDATED -> return next == WeightedTireSegmentStatus.PUBLISHED || next == WeightedTireSegmentStatus.RETIRED
            WeightedTireSegmentStatus.PUBLISHED -> {
                when (next) {
                    WeightedTireSegmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedTireSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted tire segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((odometer.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedTireSegment {
        return WeightedTireSegment(
            code = code,
            name = name,
            updatedAt = updatedAt,
            odometer = odometer,
            tags = tags,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted tire segment in status created. */
fun newWeightedTireSegment(code: String, name: String): WeightedTireSegment {
    return WeightedTireSegment(
        code = code,
        name = name,
        updatedAt = 0,
        odometer = 0,
        tags = mutableListOf<String>(),
        summary = "",
        status = WeightedTireSegmentStatus.CREATED,
    )
}
