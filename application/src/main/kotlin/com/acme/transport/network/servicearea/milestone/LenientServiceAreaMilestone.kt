package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient service area milestone records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a lenient service area milestone is in its life cycle. */
enum class LenientServiceAreaMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient service area milestone, with what the planners need to act on it. */
data class LenientServiceAreaMilestone(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var shiftCode: String,
    var depot: String,
    var trailerId: String,
    var quantity: Int,
    var channel: String,
    var summary: String,
    var visible: Boolean,
    var notes: String,
    var status: LenientServiceAreaMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServiceAreaMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServiceAreaMilestoneStatus): Boolean {
        when (status) {
            LenientServiceAreaMilestoneStatus.CREATED -> {
                when (next) {
                    LenientServiceAreaMilestoneStatus.VALIDATED, LenientServiceAreaMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            LenientServiceAreaMilestoneStatus.VALIDATED -> return next == LenientServiceAreaMilestoneStatus.PUBLISHED || next == LenientServiceAreaMilestoneStatus.RETIRED
            LenientServiceAreaMilestoneStatus.PUBLISHED -> return next == LenientServiceAreaMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientServiceAreaMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("lenient service area milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lenient service area milestone"} $code"

    fun adjustedExposure(): Double = round((quantity.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LenientServiceAreaMilestone {
        return LenientServiceAreaMilestone(
            code = code,
            label = label,
            updatedAt = updatedAt,
            shiftCode = shiftCode,
            depot = depot,
            trailerId = trailerId,
            quantity = quantity,
            channel = channel,
            summary = summary,
            visible = visible,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient service area milestone in status created. */
fun newLenientServiceAreaMilestone(code: String, label: String): LenientServiceAreaMilestone {
    return LenientServiceAreaMilestone(
        code = code,
        label = label,
        updatedAt = 0,
        shiftCode = "",
        depot = "",
        trailerId = "",
        quantity = 0,
        channel = "",
        summary = "",
        visible = false,
        notes = "",
        status = LenientServiceAreaMilestoneStatus.CREATED,
    )
}
