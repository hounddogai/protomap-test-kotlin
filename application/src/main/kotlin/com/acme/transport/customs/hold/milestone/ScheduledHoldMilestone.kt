package com.acme.transport.customs.hold.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled hold milestone records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a scheduled hold milestone is in its life cycle. */
enum class ScheduledHoldMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled hold milestone. Its code is unique within a tenant. */
data class ScheduledHoldMilestone(
    var code: String,
    var title: String,
    var startsAt: Int,
    var active: Boolean,
    var carrier: String,
    var priority: Int,
    var minimum: Double,
    var distanceKm: Double,
    var status: ScheduledHoldMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldMilestoneStatus): Boolean {
        when (status) {
            ScheduledHoldMilestoneStatus.NEW -> {
                when (next) {
                    ScheduledHoldMilestoneStatus.RUNNING -> return true
                }
                return false
            }
            ScheduledHoldMilestoneStatus.QUEUED -> {
                when (next) {
                    ScheduledHoldMilestoneStatus.RUNNING, ScheduledHoldMilestoneStatus.DONE -> return true
                }
                return false
            }
            ScheduledHoldMilestoneStatus.RUNNING -> {
                when (next) {
                    ScheduledHoldMilestoneStatus.FAILED -> return true
                }
                return false
            }
            ScheduledHoldMilestoneStatus.DONE -> return next == ScheduledHoldMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledHoldMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveLoad(): Double = round((startsAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): ScheduledHoldMilestone {
        return ScheduledHoldMilestone(
            code = code,
            title = title,
            startsAt = startsAt,
            active = active,
            carrier = carrier,
            priority = priority,
            minimum = minimum,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled hold milestone in status new. */
fun newScheduledHoldMilestone(code: String, title: String): ScheduledHoldMilestone {
    return ScheduledHoldMilestone(
        code = code,
        title = title,
        startsAt = 0,
        active = false,
        carrier = "",
        priority = 0,
        minimum = 0.0,
        distanceKm = 0.0,
        status = ScheduledHoldMilestoneStatus.NEW,
    )
}
