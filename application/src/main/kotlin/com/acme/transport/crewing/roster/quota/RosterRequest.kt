package com.acme.transport.crewing.roster.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster request is in its life cycle. */
enum class RosterRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One roster request, with what the planners need to act on it. */
data class RosterRequest(
    var code: String,
    var label: String,
    var capacity: Int,
    var ceiling: Double,
    var windowDays: Int,
    var contactEmail: String,
    var status: RosterRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterRequestStatus): Boolean {
        when (status) {
            RosterRequestStatus.DRAFT -> return next == RosterRequestStatus.PENDING || next == RosterRequestStatus.CLOSED
            RosterRequestStatus.PENDING -> {
                when (next) {
                    RosterRequestStatus.ACTIVE, RosterRequestStatus.CLOSED -> return true
                }
                return false
            }
            RosterRequestStatus.ACTIVE -> return next == RosterRequestStatus.SUSPENDED
            RosterRequestStatus.SUSPENDED -> {
                when (next) {
                    RosterRequestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterRequestStatus) {
        if (!canMoveTo(next)) {
            error("roster request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round((capacity.toDouble() + ceiling) * 100) / 100

    fun snapshot(): RosterRequest {
        return RosterRequest(
            code = code,
            label = label,
            capacity = capacity,
            ceiling = ceiling,
            windowDays = windowDays,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster request in status draft. */
fun newRosterRequest(code: String, label: String): RosterRequest {
    return RosterRequest(
        code = code,
        label = label,
        capacity = 0,
        ceiling = 0.0,
        windowDays = 0,
        contactEmail = "",
        status = RosterRequestStatus.DRAFT,
    )
}
