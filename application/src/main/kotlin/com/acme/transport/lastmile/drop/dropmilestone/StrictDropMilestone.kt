package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict drop milestone records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a strict drop milestone is in its life cycle. */
enum class StrictDropMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One strict drop milestone, with what the planners need to act on it. */
data class StrictDropMilestone(
    var code: String,
    var name: String,
    var windowDays: Int,
    var rate: Double,
    var discount: Double,
    var locale: String,
    var phone: String,
    var segment: String,
    var status: StrictDropMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDropMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDropMilestoneStatus): Boolean {
        when (status) {
            StrictDropMilestoneStatus.NEW -> return next == StrictDropMilestoneStatus.QUEUED || next == StrictDropMilestoneStatus.FAILED
            StrictDropMilestoneStatus.QUEUED -> return next == StrictDropMilestoneStatus.RUNNING || next == StrictDropMilestoneStatus.FAILED
            StrictDropMilestoneStatus.RUNNING -> {
                when (next) {
                    StrictDropMilestoneStatus.DONE, StrictDropMilestoneStatus.FAILED -> return true
                }
                return false
            }
            StrictDropMilestoneStatus.DONE -> return next == StrictDropMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictDropMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("strict drop milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedExposure(): Double = round((discount - rate) * 100) / 100

    fun snapshot(): StrictDropMilestone {
        return StrictDropMilestone(
            code = code,
            name = name,
            windowDays = windowDays,
            rate = rate,
            discount = discount,
            locale = locale,
            phone = phone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict drop milestone in status new. */
fun newStrictDropMilestone(code: String, name: String): StrictDropMilestone {
    return StrictDropMilestone(
        code = code,
        name = name,
        windowDays = 0,
        rate = 0.0,
        discount = 0.0,
        locale = "",
        phone = "",
        segment = "",
        status = StrictDropMilestoneStatus.NEW,
    )
}
