package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.text.normalize

/** Where a weighted dwell segment is in its life cycle. */
enum class WeightedDwellSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted dwell segment as the dwell team keeps it. */
data class WeightedDwellSegment(
    var code: String,
    var name: String,
    var checksum: String,
    var region: String,
    var carrier: String,
    var externalRef: String,
    var score: Double,
    var status: WeightedDwellSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDwellSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDwellSegmentStatus): Boolean {
        when (status) {
            WeightedDwellSegmentStatus.OPEN -> {
                when (next) {
                    WeightedDwellSegmentStatus.REOPENED, WeightedDwellSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedDwellSegmentStatus.IN_REVIEW -> {
                when (next) {
                    WeightedDwellSegmentStatus.REOPENED -> return true
                }
                return false
            }
            WeightedDwellSegmentStatus.RESOLVED -> return next == WeightedDwellSegmentStatus.REOPENED || next == WeightedDwellSegmentStatus.ARCHIVED
            WeightedDwellSegmentStatus.REOPENED -> return next == WeightedDwellSegmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedDwellSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted dwell segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted dwell segment"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): WeightedDwellSegment {
        return WeightedDwellSegment(
            code = code,
            name = name,
            checksum = checksum,
            region = region,
            carrier = carrier,
            externalRef = externalRef,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted dwell segment in status open. */
fun newWeightedDwellSegment(code: String, name: String): WeightedDwellSegment {
    return WeightedDwellSegment(
        code = code,
        name = name,
        checksum = "",
        region = "",
        carrier = "",
        externalRef = "",
        score = 0.0,
        status = WeightedDwellSegmentStatus.OPEN,
    )
}
