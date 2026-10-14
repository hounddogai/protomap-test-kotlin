package com.acme.transport.crewing.availability.defaultavailabilitysegment

import com.acme.transport.kit.text.normalize

/** Where a strict availability exception is in its life cycle. */
enum class StrictAvailabilityExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a strict availability exception and its current status. */
data class StrictAvailabilityException(
    var code: String,
    var label: String,
    var timezone: String,
    var shiftCode: String,
    var expiresAt: Int,
    var status: StrictAvailabilityExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAvailabilityExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAvailabilityExceptionStatus): Boolean {
        when (status) {
            StrictAvailabilityExceptionStatus.DRAFT -> return next == StrictAvailabilityExceptionStatus.SUSPENDED || next == StrictAvailabilityExceptionStatus.CLOSED
            StrictAvailabilityExceptionStatus.PENDING -> return next == StrictAvailabilityExceptionStatus.SUSPENDED
            StrictAvailabilityExceptionStatus.ACTIVE -> return next == StrictAvailabilityExceptionStatus.SUSPENDED || next == StrictAvailabilityExceptionStatus.CLOSED
            StrictAvailabilityExceptionStatus.SUSPENDED -> {
                when (next) {
                    StrictAvailabilityExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAvailabilityExceptionStatus) {
        if (!canMoveTo(next)) {
            error("strict availability exception $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict availability exception"} $code"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): StrictAvailabilityException {
        return StrictAvailabilityException(
            code = code,
            label = label,
            timezone = timezone,
            shiftCode = shiftCode,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict availability exception in status draft. */
fun newStrictAvailabilityException(code: String, label: String): StrictAvailabilityException {
    return StrictAvailabilityException(
        code = code,
        label = label,
        timezone = "",
        shiftCode = "",
        expiresAt = 0,
        status = StrictAvailabilityExceptionStatus.DRAFT,
    )
}
