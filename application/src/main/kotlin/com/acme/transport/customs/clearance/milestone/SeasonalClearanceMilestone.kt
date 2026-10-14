package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal clearance milestone records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a seasonal clearance milestone is in its life cycle. */
enum class SeasonalClearanceMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal clearance milestone and its current status. */
data class SeasonalClearanceMilestone(
    var code: String,
    var name: String,
    var archived: Boolean,
    var locale: String,
    var capacity: Int,
    var source: String,
    var budget: Double,
    var windowDays: Int,
    var phone: String,
    var startsAt: Int,
    var status: SeasonalClearanceMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalClearanceMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalClearanceMilestoneStatus): Boolean {
        when (status) {
            SeasonalClearanceMilestoneStatus.CREATED -> return next == SeasonalClearanceMilestoneStatus.VALIDATED || next == SeasonalClearanceMilestoneStatus.PUBLISHED
            SeasonalClearanceMilestoneStatus.VALIDATED -> {
                when (next) {
                    SeasonalClearanceMilestoneStatus.PUBLISHED, SeasonalClearanceMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            SeasonalClearanceMilestoneStatus.PUBLISHED -> return next == SeasonalClearanceMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalClearanceMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("seasonal clearance milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal clearance milestone"} $code"

    fun netValue(): Double = round((windowDays.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalClearanceMilestone {
        return SeasonalClearanceMilestone(
            code = code,
            name = name,
            archived = archived,
            locale = locale,
            capacity = capacity,
            source = source,
            budget = budget,
            windowDays = windowDays,
            phone = phone,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal clearance milestone in status created. */
fun newSeasonalClearanceMilestone(code: String, name: String): SeasonalClearanceMilestone {
    return SeasonalClearanceMilestone(
        code = code,
        name = name,
        archived = false,
        locale = "",
        capacity = 0,
        source = "",
        budget = 0.0,
        windowDays = 0,
        phone = "",
        startsAt = 0,
        status = SeasonalClearanceMilestoneStatus.CREATED,
    )
}
