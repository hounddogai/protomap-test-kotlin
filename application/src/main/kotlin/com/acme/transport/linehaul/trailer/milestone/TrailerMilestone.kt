package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer milestone records of linehaul's trailer work, and the rules, stores, and calculations around
// them.

/** Where a trailer milestone is in its life cycle. */
enum class TrailerMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One trailer milestone, with what the planners need to act on it. */
data class TrailerMilestone(
    var code: String,
    var title: String,
    var shiftCode: String,
    var locale: String,
    var fuelLiters: Double,
    var summary: String,
    var category: String,
    var rate: Double,
    var graceDays: Int,
    var status: TrailerMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerMilestoneStatus): Boolean {
        when (status) {
            TrailerMilestoneStatus.REQUESTED -> return next == TrailerMilestoneStatus.GRANTED
            TrailerMilestoneStatus.GRANTED -> return next == TrailerMilestoneStatus.REVOKED || next == TrailerMilestoneStatus.EXPIRED
            TrailerMilestoneStatus.REVOKED -> {
                when (next) {
                    TrailerMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("trailer milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round((graceDays.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): TrailerMilestone {
        return TrailerMilestone(
            code = code,
            title = title,
            shiftCode = shiftCode,
            locale = locale,
            fuelLiters = fuelLiters,
            summary = summary,
            category = category,
            rate = rate,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a trailer milestone in status requested. */
fun newTrailerMilestone(code: String, title: String): TrailerMilestone {
    return TrailerMilestone(
        code = code,
        title = title,
        shiftCode = "",
        locale = "",
        fuelLiters = 0.0,
        summary = "",
        category = "",
        rate = 0.0,
        graceDays = 0,
        status = TrailerMilestoneStatus.REQUESTED,
    )
}
