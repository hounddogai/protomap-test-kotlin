package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending attempt entry is in its life cycle. */
enum class PendingAttemptEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending attempt entry as the attempt team keeps it. */
data class PendingAttemptEntry(
    var code: String,
    var name: String,
    var budget: Double,
    var shiftCode: String,
    var title: String,
    var floorValue: Double,
    var label: String,
    var trailerId: String,
    var reviewer: String,
    var status: PendingAttemptEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAttemptEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAttemptEntryStatus): Boolean {
        when (status) {
            PendingAttemptEntryStatus.IDLE -> {
                when (next) {
                    PendingAttemptEntryStatus.READY -> return true
                }
                return false
            }
            PendingAttemptEntryStatus.WARMING -> return next == PendingAttemptEntryStatus.STOPPED
            PendingAttemptEntryStatus.READY -> {
                when (next) {
                    PendingAttemptEntryStatus.DRAINING, PendingAttemptEntryStatus.STOPPED -> return true
                }
                return false
            }
            PendingAttemptEntryStatus.DRAINING -> return next == PendingAttemptEntryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingAttemptEntryStatus) {
        if (!canMoveTo(next)) {
            error("pending attempt entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending attempt entry"} $code"

    fun effectiveTotal(): Double = round((floorValue - budget) * 100) / 100

    fun snapshot(): PendingAttemptEntry {
        return PendingAttemptEntry(
            code = code,
            name = name,
            budget = budget,
            shiftCode = shiftCode,
            title = title,
            floorValue = floorValue,
            label = label,
            trailerId = trailerId,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending attempt entry in status idle. */
fun newPendingAttemptEntry(code: String, name: String): PendingAttemptEntry {
    return PendingAttemptEntry(
        code = code,
        name = name,
        budget = 0.0,
        shiftCode = "",
        title = "",
        floorValue = 0.0,
        label = "",
        trailerId = "",
        reviewer = "",
        status = PendingAttemptEntryStatus.IDLE,
    )
}
