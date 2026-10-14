package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary backhaul milestone is in its life cycle. */
enum class PrimaryBackhaulMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary backhaul milestone, with what the planners need to act on it. */
data class PrimaryBackhaulMilestone(
    var code: String,
    var name: String,
    var ceiling: Double,
    var timezone: String,
    var capacity: Int,
    var status: PrimaryBackhaulMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBackhaulMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBackhaulMilestoneStatus): Boolean {
        when (status) {
            PrimaryBackhaulMilestoneStatus.NEW -> return next == PrimaryBackhaulMilestoneStatus.RUNNING || next == PrimaryBackhaulMilestoneStatus.DONE
            PrimaryBackhaulMilestoneStatus.QUEUED -> {
                when (next) {
                    PrimaryBackhaulMilestoneStatus.DONE, PrimaryBackhaulMilestoneStatus.FAILED -> return true
                }
                return false
            }
            PrimaryBackhaulMilestoneStatus.RUNNING -> return next == PrimaryBackhaulMilestoneStatus.DONE
            PrimaryBackhaulMilestoneStatus.DONE -> return next == PrimaryBackhaulMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryBackhaulMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("primary backhaul milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary backhaul milestone"} $code"

    fun adjustedTotal(): Double = round((ceiling - capacity.toDouble()) * 100) / 100

    fun snapshot(): PrimaryBackhaulMilestone {
        return PrimaryBackhaulMilestone(
            code = code,
            name = name,
            ceiling = ceiling,
            timezone = timezone,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary backhaul milestone in status new. */
fun newPrimaryBackhaulMilestone(code: String, name: String): PrimaryBackhaulMilestone {
    return PrimaryBackhaulMilestone(
        code = code,
        name = name,
        ceiling = 0.0,
        timezone = "",
        capacity = 0,
        status = PrimaryBackhaulMilestoneStatus.NEW,
    )
}
