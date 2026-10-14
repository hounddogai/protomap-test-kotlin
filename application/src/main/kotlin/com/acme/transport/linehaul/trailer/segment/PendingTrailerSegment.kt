package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending trailer segment records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a pending trailer segment is in its life cycle. */
enum class PendingTrailerSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending trailer segment as the trailer team keeps it. */
data class PendingTrailerSegment(
    var code: String,
    var label: String,
    var cost: Double,
    var discount: Double,
    var weight: Double,
    var trailerId: String,
    var summary: String,
    var rate: Double,
    var margin: Double,
    var status: PendingTrailerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTrailerSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTrailerSegmentStatus): Boolean {
        when (status) {
            PendingTrailerSegmentStatus.CREATED -> {
                when (next) {
                    PendingTrailerSegmentStatus.PUBLISHED, PendingTrailerSegmentStatus.RETIRED -> return true
                }
                return false
            }
            PendingTrailerSegmentStatus.VALIDATED -> return next == PendingTrailerSegmentStatus.PUBLISHED || next == PendingTrailerSegmentStatus.RETIRED
            PendingTrailerSegmentStatus.PUBLISHED -> return next == PendingTrailerSegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingTrailerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("pending trailer segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending trailer segment"} $code"

    fun adjustedTotal(): Double = round(discount * rate * 100) / 100

    fun snapshot(): PendingTrailerSegment {
        return PendingTrailerSegment(
            code = code,
            label = label,
            cost = cost,
            discount = discount,
            weight = weight,
            trailerId = trailerId,
            summary = summary,
            rate = rate,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending trailer segment in status created. */
fun newPendingTrailerSegment(code: String, label: String): PendingTrailerSegment {
    return PendingTrailerSegment(
        code = code,
        label = label,
        cost = 0.0,
        discount = 0.0,
        weight = 0.0,
        trailerId = "",
        summary = "",
        rate = 0.0,
        margin = 0.0,
        status = PendingTrailerSegmentStatus.CREATED,
    )
}
