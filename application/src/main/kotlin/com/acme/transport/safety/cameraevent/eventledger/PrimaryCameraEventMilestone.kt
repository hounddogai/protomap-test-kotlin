package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary camera event milestone is in its life cycle. */
enum class PrimaryCameraEventMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a primary camera event milestone and its current status. */
data class PrimaryCameraEventMilestone(
    var code: String,
    var title: String,
    var limit: Int,
    var cost: Double,
    var durationMinutes: Int,
    var status: PrimaryCameraEventMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCameraEventMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCameraEventMilestoneStatus): Boolean {
        when (status) {
            PrimaryCameraEventMilestoneStatus.DRAFT -> return next == PrimaryCameraEventMilestoneStatus.PENDING
            PrimaryCameraEventMilestoneStatus.PENDING -> return next == PrimaryCameraEventMilestoneStatus.ACTIVE || next == PrimaryCameraEventMilestoneStatus.CLOSED
            PrimaryCameraEventMilestoneStatus.ACTIVE -> return next == PrimaryCameraEventMilestoneStatus.SUSPENDED
            PrimaryCameraEventMilestoneStatus.SUSPENDED -> return next == PrimaryCameraEventMilestoneStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryCameraEventMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("primary camera event milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary camera event milestone"} $code"

    fun effectiveValue(): Double = round((cost + limit.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCameraEventMilestone {
        return PrimaryCameraEventMilestone(
            code = code,
            title = title,
            limit = limit,
            cost = cost,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary camera event milestone in status draft. */
fun newPrimaryCameraEventMilestone(code: String, title: String): PrimaryCameraEventMilestone {
    return PrimaryCameraEventMilestone(
        code = code,
        title = title,
        limit = 0,
        cost = 0.0,
        durationMinutes = 0,
        status = PrimaryCameraEventMilestoneStatus.DRAFT,
    )
}
