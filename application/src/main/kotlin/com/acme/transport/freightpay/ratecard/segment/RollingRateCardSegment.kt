package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling rate card segment records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a rolling rate card segment is in its life cycle. */
enum class RollingRateCardSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling rate card segment. Its code is unique within a tenant. */
data class RollingRateCardSegment(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var trailerId: String,
    var threshold: Double,
    var attributes: MutableMap<String, String>,
    var segment: String,
    var cost: Double,
    var pinned: Boolean,
    var status: RollingRateCardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRateCardSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRateCardSegmentStatus): Boolean {
        when (status) {
            RollingRateCardSegmentStatus.OPEN -> return next == RollingRateCardSegmentStatus.ARCHIVED
            RollingRateCardSegmentStatus.IN_REVIEW -> return next == RollingRateCardSegmentStatus.RESOLVED
            RollingRateCardSegmentStatus.RESOLVED -> return next == RollingRateCardSegmentStatus.REOPENED || next == RollingRateCardSegmentStatus.ARCHIVED
            RollingRateCardSegmentStatus.REOPENED -> return next == RollingRateCardSegmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingRateCardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling rate card segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((cost + distanceKm) * 100) / 100

    fun snapshot(): RollingRateCardSegment {
        return RollingRateCardSegment(
            code = code,
            title = title,
            distanceKm = distanceKm,
            trailerId = trailerId,
            threshold = threshold,
            attributes = attributes,
            segment = segment,
            cost = cost,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling rate card segment in status open. */
fun newRollingRateCardSegment(code: String, title: String): RollingRateCardSegment {
    return RollingRateCardSegment(
        code = code,
        title = title,
        distanceKm = 0.0,
        trailerId = "",
        threshold = 0.0,
        attributes = mutableMapOf<String, String>(),
        segment = "",
        cost = 0.0,
        pinned = false,
        status = RollingRateCardSegmentStatus.OPEN,
    )
}
