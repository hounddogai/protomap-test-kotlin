package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary locker segment is in its life cycle. */
enum class PrimaryLockerSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary locker segment as the locker team keeps it. */
data class PrimaryLockerSegment(
    var code: String,
    var name: String,
    var archived: Boolean,
    var createdAt: Int,
    var threshold: Double,
    var lane: String,
    var status: PrimaryLockerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLockerSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLockerSegmentStatus): Boolean {
        when (status) {
            PrimaryLockerSegmentStatus.DRAFT -> return next == PrimaryLockerSegmentStatus.PENDING || next == PrimaryLockerSegmentStatus.CLOSED
            PrimaryLockerSegmentStatus.PENDING -> return next == PrimaryLockerSegmentStatus.ACTIVE
            PrimaryLockerSegmentStatus.ACTIVE -> return next == PrimaryLockerSegmentStatus.SUSPENDED || next == PrimaryLockerSegmentStatus.CLOSED
            PrimaryLockerSegmentStatus.SUSPENDED -> return next == PrimaryLockerSegmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryLockerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("primary locker segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary locker segment"} $code"

    fun weightedLoad(): Double = round(createdAt.toDouble() * threshold * 100) / 100

    fun snapshot(): PrimaryLockerSegment {
        return PrimaryLockerSegment(
            code = code,
            name = name,
            archived = archived,
            createdAt = createdAt,
            threshold = threshold,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary locker segment in status draft. */
fun newPrimaryLockerSegment(code: String, name: String): PrimaryLockerSegment {
    return PrimaryLockerSegment(
        code = code,
        name = name,
        archived = false,
        createdAt = 0,
        threshold = 0.0,
        lane = "",
        status = PrimaryLockerSegmentStatus.DRAFT,
    )
}
