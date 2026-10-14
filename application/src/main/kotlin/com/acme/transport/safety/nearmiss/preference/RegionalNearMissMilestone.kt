package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional near miss milestone is in its life cycle. */
enum class RegionalNearMissMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional near miss milestone and its current status. */
data class RegionalNearMissMilestone(
    var code: String,
    var name: String,
    var segment: String,
    var lane: String,
    var amount: Double,
    var active: Boolean,
    var limit: Int,
    var ownerId: String,
    var shiftCode: String,
    var archived: Boolean,
    var status: RegionalNearMissMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalNearMissMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalNearMissMilestoneStatus): Boolean {
        when (status) {
            RegionalNearMissMilestoneStatus.CREATED -> {
                when (next) {
                    RegionalNearMissMilestoneStatus.VALIDATED, RegionalNearMissMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            RegionalNearMissMilestoneStatus.VALIDATED -> {
                when (next) {
                    RegionalNearMissMilestoneStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalNearMissMilestoneStatus.PUBLISHED -> return next == RegionalNearMissMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalNearMissMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("regional near miss milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round((amount + limit.toDouble()) * 100) / 100

    fun snapshot(): RegionalNearMissMilestone {
        return RegionalNearMissMilestone(
            code = code,
            name = name,
            segment = segment,
            lane = lane,
            amount = amount,
            active = active,
            limit = limit,
            ownerId = ownerId,
            shiftCode = shiftCode,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional near miss milestone in status created. */
fun newRegionalNearMissMilestone(code: String, name: String): RegionalNearMissMilestone {
    return RegionalNearMissMilestone(
        code = code,
        name = name,
        segment = "",
        lane = "",
        amount = 0.0,
        active = false,
        limit = 0,
        ownerId = "",
        shiftCode = "",
        archived = false,
        status = RegionalNearMissMilestoneStatus.CREATED,
    )
}
