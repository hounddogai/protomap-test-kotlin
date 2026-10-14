package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal drop milestone records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a seasonal drop milestone is in its life cycle. */
enum class SeasonalDropMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal drop milestone and its current status. */
data class SeasonalDropMilestone(
    var code: String,
    var label: String,
    var rate: Double,
    var batchSize: Int,
    var cost: Double,
    var shiftCode: String,
    var odometer: Int,
    var attempts: Int,
    var name: String,
    var status: SeasonalDropMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDropMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDropMilestoneStatus): Boolean {
        when (status) {
            SeasonalDropMilestoneStatus.OPEN -> {
                when (next) {
                    SeasonalDropMilestoneStatus.IN_REVIEW, SeasonalDropMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalDropMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalDropMilestoneStatus.RESOLVED, SeasonalDropMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalDropMilestoneStatus.RESOLVED -> return next == SeasonalDropMilestoneStatus.REOPENED || next == SeasonalDropMilestoneStatus.ARCHIVED
            SeasonalDropMilestoneStatus.REOPENED -> {
                when (next) {
                    SeasonalDropMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDropMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("seasonal drop milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedVolume(): Double = round(batchSize.toDouble() * cost * 100) / 100

    fun snapshot(): SeasonalDropMilestone {
        return SeasonalDropMilestone(
            code = code,
            label = label,
            rate = rate,
            batchSize = batchSize,
            cost = cost,
            shiftCode = shiftCode,
            odometer = odometer,
            attempts = attempts,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal drop milestone in status open. */
fun newSeasonalDropMilestone(code: String, label: String): SeasonalDropMilestone {
    return SeasonalDropMilestone(
        code = code,
        label = label,
        rate = 0.0,
        batchSize = 0,
        cost = 0.0,
        shiftCode = "",
        odometer = 0,
        attempts = 0,
        name = "",
        status = SeasonalDropMilestoneStatus.OPEN,
    )
}
