package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending transit time milestone is in its life cycle. */
enum class PendingTransitTimeMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending transit time milestone as the transit time team keeps it. */
data class PendingTransitTimeMilestone(
    var code: String,
    var name: String,
    var carrier: String,
    var quantity: Int,
    var channel: String,
    var maximum: Double,
    var status: PendingTransitTimeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTransitTimeMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTransitTimeMilestoneStatus): Boolean {
        when (status) {
            PendingTransitTimeMilestoneStatus.REQUESTED -> return next == PendingTransitTimeMilestoneStatus.EXPIRED
            PendingTransitTimeMilestoneStatus.GRANTED -> return next == PendingTransitTimeMilestoneStatus.REVOKED
            PendingTransitTimeMilestoneStatus.REVOKED -> return next == PendingTransitTimeMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingTransitTimeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("pending transit time milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending transit time milestone"} $code"

    fun weightedVolume(): Double = round((quantity.toDouble() - maximum) * 100) / 100

    fun snapshot(): PendingTransitTimeMilestone {
        return PendingTransitTimeMilestone(
            code = code,
            name = name,
            carrier = carrier,
            quantity = quantity,
            channel = channel,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending transit time milestone in status requested. */
fun newPendingTransitTimeMilestone(code: String, name: String): PendingTransitTimeMilestone {
    return PendingTransitTimeMilestone(
        code = code,
        name = name,
        carrier = "",
        quantity = 0,
        channel = "",
        maximum = 0.0,
        status = PendingTransitTimeMilestoneStatus.REQUESTED,
    )
}
