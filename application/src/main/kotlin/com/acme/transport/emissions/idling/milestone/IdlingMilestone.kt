package com.acme.transport.emissions.idling.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling milestone records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a idling milestone is in its life cycle. */
enum class IdlingMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A idling milestone as the idling team keeps it. */
data class IdlingMilestone(
    var code: String,
    var title: String,
    var maximum: Double,
    var category: String,
    var etaMinutes: Int,
    var checksum: String,
    var weight: Double,
    var contactEmail: String,
    var driverId: String,
    var status: IdlingMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingMilestoneStatus): Boolean {
        when (status) {
            IdlingMilestoneStatus.PLANNED -> return next == IdlingMilestoneStatus.IN_PROGRESS || next == IdlingMilestoneStatus.CANCELLED
            IdlingMilestoneStatus.SCHEDULED -> {
                when (next) {
                    IdlingMilestoneStatus.IN_PROGRESS -> return true
                }
                return false
            }
            IdlingMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    IdlingMilestoneStatus.COMPLETED, IdlingMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            IdlingMilestoneStatus.COMPLETED -> {
                when (next) {
                    IdlingMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("idling milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedVolume(): Double = round((maximum + weight) * 100) / 100

    fun snapshot(): IdlingMilestone {
        return IdlingMilestone(
            code = code,
            title = title,
            maximum = maximum,
            category = category,
            etaMinutes = etaMinutes,
            checksum = checksum,
            weight = weight,
            contactEmail = contactEmail,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a idling milestone in status planned. */
fun newIdlingMilestone(code: String, title: String): IdlingMilestone {
    return IdlingMilestone(
        code = code,
        title = title,
        maximum = 0.0,
        category = "",
        etaMinutes = 0,
        checksum = "",
        weight = 0.0,
        contactEmail = "",
        driverId = "",
        status = IdlingMilestoneStatus.PLANNED,
    )
}
