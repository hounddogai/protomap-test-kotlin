package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient attempt request records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a lenient attempt request is in its life cycle. */
enum class LenientAttemptRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lenient attempt request, with what the planners need to act on it. */
data class LenientAttemptRequest(
    var code: String,
    var name: String,
    var source: String,
    var startsAt: Int,
    var amount: Double,
    var rate: Double,
    var cost: Double,
    var title: String,
    var timezone: String,
    var externalRef: String,
    var status: LenientAttemptRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAttemptRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAttemptRequestStatus): Boolean {
        when (status) {
            LenientAttemptRequestStatus.PLANNED -> {
                when (next) {
                    LenientAttemptRequestStatus.SCHEDULED, LenientAttemptRequestStatus.CANCELLED -> return true
                }
                return false
            }
            LenientAttemptRequestStatus.SCHEDULED -> {
                when (next) {
                    LenientAttemptRequestStatus.COMPLETED -> return true
                }
                return false
            }
            LenientAttemptRequestStatus.IN_PROGRESS -> return next == LenientAttemptRequestStatus.COMPLETED
            LenientAttemptRequestStatus.COMPLETED -> {
                when (next) {
                    LenientAttemptRequestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAttemptRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient attempt request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round(startsAt.toDouble() * amount * 100) / 100

    fun snapshot(): LenientAttemptRequest {
        return LenientAttemptRequest(
            code = code,
            name = name,
            source = source,
            startsAt = startsAt,
            amount = amount,
            rate = rate,
            cost = cost,
            title = title,
            timezone = timezone,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient attempt request in status planned. */
fun newLenientAttemptRequest(code: String, name: String): LenientAttemptRequest {
    return LenientAttemptRequest(
        code = code,
        name = name,
        source = "",
        startsAt = 0,
        amount = 0.0,
        rate = 0.0,
        cost = 0.0,
        title = "",
        timezone = "",
        externalRef = "",
        status = LenientAttemptRequestStatus.PLANNED,
    )
}
