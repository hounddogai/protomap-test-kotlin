package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling attempt exception records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a rolling attempt exception is in its life cycle. */
enum class RollingAttemptExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling attempt exception and its current status. */
data class RollingAttemptException(
    var code: String,
    var name: String,
    var odometer: Int,
    var contactEmail: String,
    var loadKg: Double,
    var shiftCode: String,
    var limit: Int,
    var visible: Boolean,
    var category: String,
    var ownerId: String,
    var pinned: Boolean,
    var status: RollingAttemptExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAttemptExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAttemptExceptionStatus): Boolean {
        when (status) {
            RollingAttemptExceptionStatus.OPEN -> {
                when (next) {
                    RollingAttemptExceptionStatus.RESOLVED -> return true
                }
                return false
            }
            RollingAttemptExceptionStatus.IN_REVIEW -> return next == RollingAttemptExceptionStatus.REOPENED
            RollingAttemptExceptionStatus.RESOLVED -> {
                when (next) {
                    RollingAttemptExceptionStatus.REOPENED, RollingAttemptExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingAttemptExceptionStatus.REOPENED -> {
                when (next) {
                    RollingAttemptExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAttemptExceptionStatus) {
        if (!canMoveTo(next)) {
            error("rolling attempt exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round(odometer.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): RollingAttemptException {
        return RollingAttemptException(
            code = code,
            name = name,
            odometer = odometer,
            contactEmail = contactEmail,
            loadKg = loadKg,
            shiftCode = shiftCode,
            limit = limit,
            visible = visible,
            category = category,
            ownerId = ownerId,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling attempt exception in status open. */
fun newRollingAttemptException(code: String, name: String): RollingAttemptException {
    return RollingAttemptException(
        code = code,
        name = name,
        odometer = 0,
        contactEmail = "",
        loadKg = 0.0,
        shiftCode = "",
        limit = 0,
        visible = false,
        category = "",
        ownerId = "",
        pinned = false,
        status = RollingAttemptExceptionStatus.OPEN,
    )
}
