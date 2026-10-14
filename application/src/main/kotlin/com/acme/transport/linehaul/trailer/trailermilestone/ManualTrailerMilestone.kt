package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual trailer milestone records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a manual trailer milestone is in its life cycle. */
enum class ManualTrailerMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual trailer milestone. Its code is unique within a tenant. */
data class ManualTrailerMilestone(
    var code: String,
    var name: String,
    var retries: Int,
    var budget: Double,
    var title: String,
    var carrier: String,
    var ceiling: Double,
    var externalRef: String,
    var status: ManualTrailerMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerMilestoneStatus): Boolean {
        when (status) {
            ManualTrailerMilestoneStatus.PLANNED -> return next == ManualTrailerMilestoneStatus.IN_PROGRESS || next == ManualTrailerMilestoneStatus.COMPLETED
            ManualTrailerMilestoneStatus.SCHEDULED -> {
                when (next) {
                    ManualTrailerMilestoneStatus.COMPLETED -> return true
                }
                return false
            }
            ManualTrailerMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    ManualTrailerMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            ManualTrailerMilestoneStatus.COMPLETED -> {
                when (next) {
                    ManualTrailerMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round(retries.toDouble() * ceiling * 100) / 100

    fun snapshot(): ManualTrailerMilestone {
        return ManualTrailerMilestone(
            code = code,
            name = name,
            retries = retries,
            budget = budget,
            title = title,
            carrier = carrier,
            ceiling = ceiling,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual trailer milestone in status planned. */
fun newManualTrailerMilestone(code: String, name: String): ManualTrailerMilestone {
    return ManualTrailerMilestone(
        code = code,
        name = name,
        retries = 0,
        budget = 0.0,
        title = "",
        carrier = "",
        ceiling = 0.0,
        externalRef = "",
        status = ManualTrailerMilestoneStatus.PLANNED,
    )
}
