package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door score records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door score is in its life cycle. */
enum class DockDoorScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a dock door score and its current status. */
data class DockDoorScore(
    var code: String,
    var name: String,
    var country: String,
    var graceDays: Int,
    var timezone: String,
    var contactEmail: String,
    var retries: Int,
    var status: DockDoorScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorScoreStatus): Boolean {
        when (status) {
            DockDoorScoreStatus.PROPOSED -> return next == DockDoorScoreStatus.APPROVED || next == DockDoorScoreStatus.REJECTED
            DockDoorScoreStatus.APPROVED -> return next == DockDoorScoreStatus.REJECTED
            DockDoorScoreStatus.REJECTED -> {
                when (next) {
                    DockDoorScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorScoreStatus) {
        if (!canMoveTo(next)) {
            error("dock door score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dock door score"} $code"

    fun adjustedLoad(): Double = round((retries.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): DockDoorScore {
        return DockDoorScore(
            code = code,
            name = name,
            country = country,
            graceDays = graceDays,
            timezone = timezone,
            contactEmail = contactEmail,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dock door score in status proposed. */
fun newDockDoorScore(code: String, name: String): DockDoorScore {
    return DockDoorScore(
        code = code,
        name = name,
        country = "",
        graceDays = 0,
        timezone = "",
        contactEmail = "",
        retries = 0,
        status = DockDoorScoreStatus.PROPOSED,
    )
}
