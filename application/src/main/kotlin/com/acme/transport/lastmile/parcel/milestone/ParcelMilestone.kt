package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel milestone records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a parcel milestone is in its life cycle. */
enum class ParcelMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A parcel milestone as the parcel team keeps it. */
data class ParcelMilestone(
    var code: String,
    var name: String,
    var loadKg: Double,
    var segment: String,
    var priority: Int,
    var status: ParcelMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelMilestoneStatus): Boolean {
        when (status) {
            ParcelMilestoneStatus.IDLE -> {
                when (next) {
                    ParcelMilestoneStatus.READY -> return true
                }
                return false
            }
            ParcelMilestoneStatus.WARMING -> return next == ParcelMilestoneStatus.DRAINING || next == ParcelMilestoneStatus.STOPPED
            ParcelMilestoneStatus.READY -> return next == ParcelMilestoneStatus.STOPPED
            ParcelMilestoneStatus.DRAINING -> {
                when (next) {
                    ParcelMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("parcel milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedVolume(): Double = round((loadKg + priority.toDouble()) * 100) / 100

    fun snapshot(): ParcelMilestone {
        return ParcelMilestone(
            code = code,
            name = name,
            loadKg = loadKg,
            segment = segment,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a parcel milestone in status idle. */
fun newParcelMilestone(code: String, name: String): ParcelMilestone {
    return ParcelMilestone(
        code = code,
        name = name,
        loadKg = 0.0,
        segment = "",
        priority = 0,
        status = ParcelMilestoneStatus.IDLE,
    )
}
