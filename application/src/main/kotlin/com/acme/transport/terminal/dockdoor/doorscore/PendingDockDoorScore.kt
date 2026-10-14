package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.kit.text.normalize

// The pending dock door score records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a pending dock door score is in its life cycle. */
enum class PendingDockDoorScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending dock door score as the dock door team keeps it. */
data class PendingDockDoorScore(
    var code: String,
    var title: String,
    var trailerId: String,
    var channel: String,
    var reviewer: String,
    var pallets: Int,
    var status: PendingDockDoorScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDockDoorScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDockDoorScoreStatus): Boolean {
        when (status) {
            PendingDockDoorScoreStatus.DRAFT -> return next == PendingDockDoorScoreStatus.SUSPENDED
            PendingDockDoorScoreStatus.PENDING -> return next == PendingDockDoorScoreStatus.ACTIVE
            PendingDockDoorScoreStatus.ACTIVE -> return next == PendingDockDoorScoreStatus.SUSPENDED || next == PendingDockDoorScoreStatus.CLOSED
            PendingDockDoorScoreStatus.SUSPENDED -> return next == PendingDockDoorScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingDockDoorScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending dock door score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending dock door score"} $code"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): PendingDockDoorScore {
        return PendingDockDoorScore(
            code = code,
            title = title,
            trailerId = trailerId,
            channel = channel,
            reviewer = reviewer,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending dock door score in status draft. */
fun newPendingDockDoorScore(code: String, title: String): PendingDockDoorScore {
    return PendingDockDoorScore(
        code = code,
        title = title,
        trailerId = "",
        channel = "",
        reviewer = "",
        pallets = 0,
        status = PendingDockDoorScoreStatus.DRAFT,
    )
}
