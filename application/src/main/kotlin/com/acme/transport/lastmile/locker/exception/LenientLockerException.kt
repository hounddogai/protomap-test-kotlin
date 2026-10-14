package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient locker exception records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a lenient locker exception is in its life cycle. */
enum class LenientLockerExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient locker exception, with what the planners need to act on it. */
data class LenientLockerException(
    var code: String,
    var label: String,
    var channel: String,
    var description: String,
    var depot: String,
    var approved: Boolean,
    var quantity: Int,
    var createdAt: Int,
    var pinned: Boolean,
    var status: LenientLockerExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLockerExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLockerExceptionStatus): Boolean {
        when (status) {
            LenientLockerExceptionStatus.CREATED -> return next == LenientLockerExceptionStatus.PUBLISHED || next == LenientLockerExceptionStatus.RETIRED
            LenientLockerExceptionStatus.VALIDATED -> return next == LenientLockerExceptionStatus.PUBLISHED || next == LenientLockerExceptionStatus.RETIRED
            LenientLockerExceptionStatus.PUBLISHED -> {
                when (next) {
                    LenientLockerExceptionStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientLockerExceptionStatus) {
        if (!canMoveTo(next)) {
            error("lenient locker exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((quantity.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): LenientLockerException {
        return LenientLockerException(
            code = code,
            label = label,
            channel = channel,
            description = description,
            depot = depot,
            approved = approved,
            quantity = quantity,
            createdAt = createdAt,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient locker exception in status created. */
fun newLenientLockerException(code: String, label: String): LenientLockerException {
    return LenientLockerException(
        code = code,
        label = label,
        channel = "",
        description = "",
        depot = "",
        approved = false,
        quantity = 0,
        createdAt = 0,
        pinned = false,
        status = LenientLockerExceptionStatus.CREATED,
    )
}
