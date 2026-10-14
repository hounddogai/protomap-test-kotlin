package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy dwell milestone is in its life cycle. */
enum class LegacyDwellMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a legacy dwell milestone and its current status. */
data class LegacyDwellMilestone(
    var code: String,
    var label: String,
    var contactEmail: String,
    var description: String,
    var currency: String,
    var durationMinutes: Int,
    var startsAt: Int,
    var lane: String,
    var priority: Int,
    var reviewer: String,
    var status: LegacyDwellMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDwellMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDwellMilestoneStatus): Boolean {
        when (status) {
            LegacyDwellMilestoneStatus.CREATED -> return next == LegacyDwellMilestoneStatus.VALIDATED || next == LegacyDwellMilestoneStatus.RETIRED
            LegacyDwellMilestoneStatus.VALIDATED -> {
                when (next) {
                    LegacyDwellMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            LegacyDwellMilestoneStatus.PUBLISHED -> {
                when (next) {
                    LegacyDwellMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDwellMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("legacy dwell milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy dwell milestone"} $code"

    fun netExposure(): Double = round((priority.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): LegacyDwellMilestone {
        return LegacyDwellMilestone(
            code = code,
            label = label,
            contactEmail = contactEmail,
            description = description,
            currency = currency,
            durationMinutes = durationMinutes,
            startsAt = startsAt,
            lane = lane,
            priority = priority,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy dwell milestone in status created. */
fun newLegacyDwellMilestone(code: String, label: String): LegacyDwellMilestone {
    return LegacyDwellMilestone(
        code = code,
        label = label,
        contactEmail = "",
        description = "",
        currency = "",
        durationMinutes = 0,
        startsAt = 0,
        lane = "",
        priority = 0,
        reviewer = "",
        status = LegacyDwellMilestoneStatus.CREATED,
    )
}
