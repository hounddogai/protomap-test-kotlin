package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard segment is in its life cycle. */
enum class TrailerYardSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A trailer yard segment. Its code is unique within a tenant. */
data class TrailerYardSegment(
    var code: String,
    var title: String,
    var budget: Double,
    var ratio: Double,
    var currency: String,
    var ownerId: String,
    var capacity: Int,
    var phone: String,
    var status: TrailerYardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardSegmentStatus): Boolean {
        when (status) {
            TrailerYardSegmentStatus.OPEN -> {
                when (next) {
                    TrailerYardSegmentStatus.IN_REVIEW, TrailerYardSegmentStatus.RESOLVED -> return true
                }
                return false
            }
            TrailerYardSegmentStatus.IN_REVIEW -> return next == TrailerYardSegmentStatus.RESOLVED || next == TrailerYardSegmentStatus.REOPENED
            TrailerYardSegmentStatus.RESOLVED -> {
                when (next) {
                    TrailerYardSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            TrailerYardSegmentStatus.REOPENED -> return next == TrailerYardSegmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TrailerYardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((ratio - capacity.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardSegment {
        return TrailerYardSegment(
            code = code,
            title = title,
            budget = budget,
            ratio = ratio,
            currency = currency,
            ownerId = ownerId,
            capacity = capacity,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer yard segment in status open. */
fun newTrailerYardSegment(code: String, title: String): TrailerYardSegment {
    return TrailerYardSegment(
        code = code,
        title = title,
        budget = 0.0,
        ratio = 0.0,
        currency = "",
        ownerId = "",
        capacity = 0,
        phone = "",
        status = TrailerYardSegmentStatus.OPEN,
    )
}
