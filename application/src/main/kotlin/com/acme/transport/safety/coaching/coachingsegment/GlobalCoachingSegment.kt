package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global coaching segment records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a global coaching segment is in its life cycle. */
enum class GlobalCoachingSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global coaching segment. Its code is unique within a tenant. */
data class GlobalCoachingSegment(
    var code: String,
    var name: String,
    var lane: String,
    var active: Boolean,
    var country: String,
    var revision: Int,
    var retries: Int,
    var batchSize: Int,
    var status: GlobalCoachingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCoachingSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCoachingSegmentStatus): Boolean {
        when (status) {
            GlobalCoachingSegmentStatus.PLANNED -> {
                when (next) {
                    GlobalCoachingSegmentStatus.SCHEDULED -> return true
                }
                return false
            }
            GlobalCoachingSegmentStatus.SCHEDULED -> {
                when (next) {
                    GlobalCoachingSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalCoachingSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalCoachingSegmentStatus.COMPLETED, GlobalCoachingSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalCoachingSegmentStatus.COMPLETED -> return next == GlobalCoachingSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalCoachingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("global coaching segment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"global coaching segment"} $code"

    fun weightedExposure(): Double = round((retries.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): GlobalCoachingSegment {
        return GlobalCoachingSegment(
            code = code,
            name = name,
            lane = lane,
            active = active,
            country = country,
            revision = revision,
            retries = retries,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global coaching segment in status planned. */
fun newGlobalCoachingSegment(code: String, name: String): GlobalCoachingSegment {
    return GlobalCoachingSegment(
        code = code,
        name = name,
        lane = "",
        active = false,
        country = "",
        revision = 0,
        retries = 0,
        batchSize = 0,
        status = GlobalCoachingSegmentStatus.PLANNED,
    )
}
