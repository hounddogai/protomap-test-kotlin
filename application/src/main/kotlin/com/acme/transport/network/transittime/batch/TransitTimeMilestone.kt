package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time milestone is in its life cycle. */
enum class TransitTimeMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a transit time milestone and its current status. */
data class TransitTimeMilestone(
    var code: String,
    var label: String,
    var ceiling: Double,
    var odometer: Int,
    var category: String,
    var durationMinutes: Int,
    var status: TransitTimeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeMilestoneStatus): Boolean {
        when (status) {
            TransitTimeMilestoneStatus.OPEN -> {
                when (next) {
                    TransitTimeMilestoneStatus.RESOLVED -> return true
                }
                return false
            }
            TransitTimeMilestoneStatus.IN_REVIEW -> return next == TransitTimeMilestoneStatus.RESOLVED || next == TransitTimeMilestoneStatus.REOPENED
            TransitTimeMilestoneStatus.RESOLVED -> {
                when (next) {
                    TransitTimeMilestoneStatus.REOPENED, TransitTimeMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            TransitTimeMilestoneStatus.REOPENED -> return next == TransitTimeMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TransitTimeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("transit time milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round(ceiling * odometer.toDouble() * 100) / 100

    fun snapshot(): TransitTimeMilestone {
        return TransitTimeMilestone(
            code = code,
            label = label,
            ceiling = ceiling,
            odometer = odometer,
            category = category,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a transit time milestone in status open. */
fun newTransitTimeMilestone(code: String, label: String): TransitTimeMilestone {
    return TransitTimeMilestone(
        code = code,
        label = label,
        ceiling = 0.0,
        odometer = 0,
        category = "",
        durationMinutes = 0,
        status = TransitTimeMilestoneStatus.OPEN,
    )
}
