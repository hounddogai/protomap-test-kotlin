package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold milestone is in its life cycle. */
enum class HoldMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A hold milestone as the hold team keeps it. */
data class HoldMilestone(
    var code: String,
    var name: String,
    var ownerId: String,
    var stopCount: Int,
    var startsAt: Int,
    var country: String,
    var carrier: String,
    var status: HoldMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldMilestoneStatus): Boolean {
        when (status) {
            HoldMilestoneStatus.REQUESTED -> return next == HoldMilestoneStatus.GRANTED || next == HoldMilestoneStatus.EXPIRED
            HoldMilestoneStatus.GRANTED -> {
                when (next) {
                    HoldMilestoneStatus.REVOKED, HoldMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            HoldMilestoneStatus.REVOKED -> return next == HoldMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HoldMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("hold milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round(stopCount.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): HoldMilestone {
        return HoldMilestone(
            code = code,
            name = name,
            ownerId = ownerId,
            stopCount = stopCount,
            startsAt = startsAt,
            country = country,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a hold milestone in status requested. */
fun newHoldMilestone(code: String, name: String): HoldMilestone {
    return HoldMilestone(
        code = code,
        name = name,
        ownerId = "",
        stopCount = 0,
        startsAt = 0,
        country = "",
        carrier = "",
        status = HoldMilestoneStatus.REQUESTED,
    )
}
