package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling near miss segment is in its life cycle. */
enum class RollingNearMissSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling near miss segment as the near miss team keeps it. */
data class RollingNearMissSegment(
    var code: String,
    var label: String,
    var margin: Double,
    var distanceKm: Double,
    var archived: Boolean,
    var contactEmail: String,
    var floorValue: Double,
    var status: RollingNearMissSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingNearMissSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingNearMissSegmentStatus): Boolean {
        when (status) {
            RollingNearMissSegmentStatus.PROPOSED -> return next == RollingNearMissSegmentStatus.REJECTED || next == RollingNearMissSegmentStatus.WITHDRAWN
            RollingNearMissSegmentStatus.APPROVED -> return next == RollingNearMissSegmentStatus.REJECTED || next == RollingNearMissSegmentStatus.WITHDRAWN
            RollingNearMissSegmentStatus.REJECTED -> return next == RollingNearMissSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingNearMissSegmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling near miss segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round((distanceKm + floorValue) * 100) / 100

    fun snapshot(): RollingNearMissSegment {
        return RollingNearMissSegment(
            code = code,
            label = label,
            margin = margin,
            distanceKm = distanceKm,
            archived = archived,
            contactEmail = contactEmail,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling near miss segment in status proposed. */
fun newRollingNearMissSegment(code: String, label: String): RollingNearMissSegment {
    return RollingNearMissSegment(
        code = code,
        label = label,
        margin = 0.0,
        distanceKm = 0.0,
        archived = false,
        contactEmail = "",
        floorValue = 0.0,
        status = RollingNearMissSegmentStatus.PROPOSED,
    )
}
