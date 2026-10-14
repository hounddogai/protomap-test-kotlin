package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending lane plan segment records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a pending lane plan segment is in its life cycle. */
enum class PendingLanePlanSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending lane plan segment as the lane plan team keeps it. */
data class PendingLanePlanSegment(
    var code: String,
    var title: String,
    var createdAt: Int,
    var active: Boolean,
    var notes: String,
    var fuelLiters: Double,
    var checksum: String,
    var durationMinutes: Int,
    var odometer: Int,
    var pinned: Boolean,
    var segment: String,
    var status: PendingLanePlanSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLanePlanSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLanePlanSegmentStatus): Boolean {
        when (status) {
            PendingLanePlanSegmentStatus.IDLE -> {
                when (next) {
                    PendingLanePlanSegmentStatus.WARMING, PendingLanePlanSegmentStatus.READY -> return true
                }
                return false
            }
            PendingLanePlanSegmentStatus.WARMING -> {
                when (next) {
                    PendingLanePlanSegmentStatus.READY -> return true
                }
                return false
            }
            PendingLanePlanSegmentStatus.READY -> return next == PendingLanePlanSegmentStatus.DRAINING
            PendingLanePlanSegmentStatus.DRAINING -> {
                when (next) {
                    PendingLanePlanSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingLanePlanSegmentStatus) {
        if (!canMoveTo(next)) {
            error("pending lane plan segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((durationMinutes.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): PendingLanePlanSegment {
        return PendingLanePlanSegment(
            code = code,
            title = title,
            createdAt = createdAt,
            active = active,
            notes = notes,
            fuelLiters = fuelLiters,
            checksum = checksum,
            durationMinutes = durationMinutes,
            odometer = odometer,
            pinned = pinned,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending lane plan segment in status idle. */
fun newPendingLanePlanSegment(code: String, title: String): PendingLanePlanSegment {
    return PendingLanePlanSegment(
        code = code,
        title = title,
        createdAt = 0,
        active = false,
        notes = "",
        fuelLiters = 0.0,
        checksum = "",
        durationMinutes = 0,
        odometer = 0,
        pinned = false,
        segment = "",
        status = PendingLanePlanSegmentStatus.IDLE,
    )
}
