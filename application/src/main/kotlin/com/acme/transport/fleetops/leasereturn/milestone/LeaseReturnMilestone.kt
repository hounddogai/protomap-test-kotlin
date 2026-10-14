package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return milestone records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return milestone is in its life cycle. */
enum class LeaseReturnMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lease return milestone and its current status. */
data class LeaseReturnMilestone(
    var code: String,
    var name: String,
    var visible: Boolean,
    var amount: Double,
    var cost: Double,
    var discount: Double,
    var score: Double,
    var durationMinutes: Int,
    var status: LeaseReturnMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnMilestoneStatus): Boolean {
        when (status) {
            LeaseReturnMilestoneStatus.NEW -> return next == LeaseReturnMilestoneStatus.FAILED
            LeaseReturnMilestoneStatus.QUEUED -> return next == LeaseReturnMilestoneStatus.RUNNING || next == LeaseReturnMilestoneStatus.DONE
            LeaseReturnMilestoneStatus.RUNNING -> {
                when (next) {
                    LeaseReturnMilestoneStatus.DONE, LeaseReturnMilestoneStatus.FAILED -> return true
                }
                return false
            }
            LeaseReturnMilestoneStatus.DONE -> {
                when (next) {
                    LeaseReturnMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("lease return milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round(durationMinutes.toDouble() * score * 100) / 100

    fun snapshot(): LeaseReturnMilestone {
        return LeaseReturnMilestone(
            code = code,
            name = name,
            visible = visible,
            amount = amount,
            cost = cost,
            discount = discount,
            score = score,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lease return milestone in status new. */
fun newLeaseReturnMilestone(code: String, name: String): LeaseReturnMilestone {
    return LeaseReturnMilestone(
        code = code,
        name = name,
        visible = false,
        amount = 0.0,
        cost = 0.0,
        discount = 0.0,
        score = 0.0,
        durationMinutes = 0,
        status = LeaseReturnMilestoneStatus.NEW,
    )
}
