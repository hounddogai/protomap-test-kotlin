package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted lease return milestone records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a weighted lease return milestone is in its life cycle. */
enum class WeightedLeaseReturnMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One weighted lease return milestone, with what the planners need to act on it. */
data class WeightedLeaseReturnMilestone(
    var code: String,
    var title: String,
    var channel: String,
    var quantity: Int,
    var approved: Boolean,
    var version: Int,
    var segment: String,
    var status: WeightedLeaseReturnMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLeaseReturnMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLeaseReturnMilestoneStatus): Boolean {
        when (status) {
            WeightedLeaseReturnMilestoneStatus.NEW -> return next == WeightedLeaseReturnMilestoneStatus.QUEUED
            WeightedLeaseReturnMilestoneStatus.QUEUED -> {
                when (next) {
                    WeightedLeaseReturnMilestoneStatus.RUNNING, WeightedLeaseReturnMilestoneStatus.DONE -> return true
                }
                return false
            }
            WeightedLeaseReturnMilestoneStatus.RUNNING -> {
                when (next) {
                    WeightedLeaseReturnMilestoneStatus.DONE, WeightedLeaseReturnMilestoneStatus.FAILED -> return true
                }
                return false
            }
            WeightedLeaseReturnMilestoneStatus.DONE -> {
                when (next) {
                    WeightedLeaseReturnMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedLeaseReturnMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("weighted lease return milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round((version.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): WeightedLeaseReturnMilestone {
        return WeightedLeaseReturnMilestone(
            code = code,
            title = title,
            channel = channel,
            quantity = quantity,
            approved = approved,
            version = version,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted lease return milestone in status new. */
fun newWeightedLeaseReturnMilestone(code: String, title: String): WeightedLeaseReturnMilestone {
    return WeightedLeaseReturnMilestone(
        code = code,
        title = title,
        channel = "",
        quantity = 0,
        approved = false,
        version = 0,
        segment = "",
        status = WeightedLeaseReturnMilestoneStatus.NEW,
    )
}
