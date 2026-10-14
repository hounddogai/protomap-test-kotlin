package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global clearance milestone is in its life cycle. */
enum class GlobalClearanceMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a global clearance milestone and its current status. */
data class GlobalClearanceMilestone(
    var code: String,
    var title: String,
    var amount: Double,
    var sequence: Int,
    var startsAt: Int,
    var status: GlobalClearanceMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalClearanceMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalClearanceMilestoneStatus): Boolean {
        when (status) {
            GlobalClearanceMilestoneStatus.REQUESTED -> return next == GlobalClearanceMilestoneStatus.EXPIRED
            GlobalClearanceMilestoneStatus.GRANTED -> {
                when (next) {
                    GlobalClearanceMilestoneStatus.REVOKED, GlobalClearanceMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalClearanceMilestoneStatus.REVOKED -> {
                when (next) {
                    GlobalClearanceMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalClearanceMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("global clearance milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global clearance milestone"} $code"

    fun weightedExposure(): Double = round(startsAt.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): GlobalClearanceMilestone {
        return GlobalClearanceMilestone(
            code = code,
            title = title,
            amount = amount,
            sequence = sequence,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global clearance milestone in status requested. */
fun newGlobalClearanceMilestone(code: String, title: String): GlobalClearanceMilestone {
    return GlobalClearanceMilestone(
        code = code,
        title = title,
        amount = 0.0,
        sequence = 0,
        startsAt = 0,
        status = GlobalClearanceMilestoneStatus.REQUESTED,
    )
}
