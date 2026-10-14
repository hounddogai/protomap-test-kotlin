package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal incident milestone is in its life cycle. */
enum class SeasonalIncidentMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One seasonal incident milestone, with what the planners need to act on it. */
data class SeasonalIncidentMilestone(
    var code: String,
    var label: String,
    var notes: String,
    var loadKg: Double,
    var timezone: String,
    var createdAt: Int,
    var stopCount: Int,
    var status: SeasonalIncidentMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalIncidentMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalIncidentMilestoneStatus): Boolean {
        when (status) {
            SeasonalIncidentMilestoneStatus.IDLE -> {
                when (next) {
                    SeasonalIncidentMilestoneStatus.WARMING -> return true
                }
                return false
            }
            SeasonalIncidentMilestoneStatus.WARMING -> {
                when (next) {
                    SeasonalIncidentMilestoneStatus.READY, SeasonalIncidentMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalIncidentMilestoneStatus.READY -> return next == SeasonalIncidentMilestoneStatus.STOPPED
            SeasonalIncidentMilestoneStatus.DRAINING -> return next == SeasonalIncidentMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalIncidentMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("seasonal incident milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round(createdAt.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): SeasonalIncidentMilestone {
        return SeasonalIncidentMilestone(
            code = code,
            label = label,
            notes = notes,
            loadKg = loadKg,
            timezone = timezone,
            createdAt = createdAt,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal incident milestone in status idle. */
fun newSeasonalIncidentMilestone(code: String, label: String): SeasonalIncidentMilestone {
    return SeasonalIncidentMilestone(
        code = code,
        label = label,
        notes = "",
        loadKg = 0.0,
        timezone = "",
        createdAt = 0,
        stopCount = 0,
        status = SeasonalIncidentMilestoneStatus.IDLE,
    )
}
