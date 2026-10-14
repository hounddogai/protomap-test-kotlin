package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending attempt request is in its life cycle. */
enum class PendingAttemptRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending attempt request, with what the planners need to act on it. */
data class PendingAttemptRequest(
    var code: String,
    var label: String,
    var attempts: Int,
    var windowDays: Int,
    var trailerId: String,
    var status: PendingAttemptRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAttemptRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAttemptRequestStatus): Boolean {
        when (status) {
            PendingAttemptRequestStatus.REQUESTED -> {
                when (next) {
                    PendingAttemptRequestStatus.EXPIRED -> return true
                }
                return false
            }
            PendingAttemptRequestStatus.GRANTED -> return next == PendingAttemptRequestStatus.REVOKED
            PendingAttemptRequestStatus.REVOKED -> {
                when (next) {
                    PendingAttemptRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAttemptRequestStatus) {
        if (!canMoveTo(next)) {
            error("pending attempt request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending attempt request"} $code"

    fun adjustedValue(): Double = round((attempts.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): PendingAttemptRequest {
        return PendingAttemptRequest(
            code = code,
            label = label,
            attempts = attempts,
            windowDays = windowDays,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending attempt request in status requested. */
fun newPendingAttemptRequest(code: String, label: String): PendingAttemptRequest {
    return PendingAttemptRequest(
        code = code,
        label = label,
        attempts = 0,
        windowDays = 0,
        trailerId = "",
        status = PendingAttemptRequestStatus.REQUESTED,
    )
}
