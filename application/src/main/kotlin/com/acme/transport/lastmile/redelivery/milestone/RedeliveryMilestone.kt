package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery milestone records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery milestone is in its life cycle. */
enum class RedeliveryMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A redelivery milestone as the redelivery team keeps it. */
data class RedeliveryMilestone(
    var code: String,
    var name: String,
    var graceDays: Int,
    var phone: String,
    var revision: Int,
    var status: RedeliveryMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryMilestoneStatus): Boolean {
        when (status) {
            RedeliveryMilestoneStatus.NEW -> return next == RedeliveryMilestoneStatus.RUNNING || next == RedeliveryMilestoneStatus.DONE
            RedeliveryMilestoneStatus.QUEUED -> return next == RedeliveryMilestoneStatus.DONE || next == RedeliveryMilestoneStatus.FAILED
            RedeliveryMilestoneStatus.RUNNING -> return next == RedeliveryMilestoneStatus.FAILED
            RedeliveryMilestoneStatus.DONE -> return next == RedeliveryMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RedeliveryMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("redelivery milestone $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun weightedExposure(): Double = round((revision.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryMilestone {
        return RedeliveryMilestone(
            code = code,
            name = name,
            graceDays = graceDays,
            phone = phone,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery milestone in status new. */
fun newRedeliveryMilestone(code: String, name: String): RedeliveryMilestone {
    return RedeliveryMilestone(
        code = code,
        name = name,
        graceDays = 0,
        phone = "",
        revision = 0,
        status = RedeliveryMilestoneStatus.NEW,
    )
}
