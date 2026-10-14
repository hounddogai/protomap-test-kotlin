package com.acme.transport.lastmile.attempt.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt request records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a attempt request is in its life cycle. */
enum class AttemptRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A attempt request as the attempt team keeps it. */
data class AttemptRequest(
    var code: String,
    var title: String,
    var minimum: Double,
    var name: String,
    var windowDays: Int,
    var pinned: Boolean,
    var graceDays: Int,
    var cost: Double,
    var floorValue: Double,
    var approved: Boolean,
    var status: AttemptRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptRequestStatus): Boolean {
        when (status) {
            AttemptRequestStatus.PROPOSED -> {
                when (next) {
                    AttemptRequestStatus.APPROVED -> return true
                }
                return false
            }
            AttemptRequestStatus.APPROVED -> return next == AttemptRequestStatus.REJECTED || next == AttemptRequestStatus.WITHDRAWN
            AttemptRequestStatus.REJECTED -> return next == AttemptRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AttemptRequestStatus) {
        if (!canMoveTo(next)) {
            error("attempt request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round(minimum * cost * 100) / 100

    fun snapshot(): AttemptRequest {
        return AttemptRequest(
            code = code,
            title = title,
            minimum = minimum,
            name = name,
            windowDays = windowDays,
            pinned = pinned,
            graceDays = graceDays,
            cost = cost,
            floorValue = floorValue,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a attempt request in status proposed. */
fun newAttemptRequest(code: String, title: String): AttemptRequest {
    return AttemptRequest(
        code = code,
        title = title,
        minimum = 0.0,
        name = "",
        windowDays = 0,
        pinned = false,
        graceDays = 0,
        cost = 0.0,
        floorValue = 0.0,
        approved = false,
        status = AttemptRequestStatus.PROPOSED,
    )
}
