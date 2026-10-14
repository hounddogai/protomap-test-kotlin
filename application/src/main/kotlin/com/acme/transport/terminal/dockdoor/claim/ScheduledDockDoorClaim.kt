package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled dock door claim records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a scheduled dock door claim is in its life cycle. */
enum class ScheduledDockDoorClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled dock door claim and its current status. */
data class ScheduledDockDoorClaim(
    var code: String,
    var name: String,
    var budget: Double,
    var priority: Int,
    var version: Int,
    var category: String,
    var status: ScheduledDockDoorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDockDoorClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDockDoorClaimStatus): Boolean {
        when (status) {
            ScheduledDockDoorClaimStatus.NEW -> return next == ScheduledDockDoorClaimStatus.RUNNING || next == ScheduledDockDoorClaimStatus.DONE
            ScheduledDockDoorClaimStatus.QUEUED -> {
                when (next) {
                    ScheduledDockDoorClaimStatus.DONE -> return true
                }
                return false
            }
            ScheduledDockDoorClaimStatus.RUNNING -> return next == ScheduledDockDoorClaimStatus.DONE || next == ScheduledDockDoorClaimStatus.FAILED
            ScheduledDockDoorClaimStatus.DONE -> return next == ScheduledDockDoorClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledDockDoorClaimStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dock door claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((priority.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDockDoorClaim {
        return ScheduledDockDoorClaim(
            code = code,
            name = name,
            budget = budget,
            priority = priority,
            version = version,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled dock door claim in status new. */
fun newScheduledDockDoorClaim(code: String, name: String): ScheduledDockDoorClaim {
    return ScheduledDockDoorClaim(
        code = code,
        name = name,
        budget = 0.0,
        priority = 0,
        version = 0,
        category = "",
        status = ScheduledDockDoorClaimStatus.NEW,
    )
}
