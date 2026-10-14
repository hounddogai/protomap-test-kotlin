package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt exception is in its life cycle. */
enum class AttemptExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One attempt exception, with what the planners need to act on it. */
data class AttemptException(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var attempts: Int,
    var priority: Int,
    var country: String,
    var timezone: String,
    var status: AttemptExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptExceptionStatus): Boolean {
        when (status) {
            AttemptExceptionStatus.REQUESTED -> return next == AttemptExceptionStatus.GRANTED
            AttemptExceptionStatus.GRANTED -> return next == AttemptExceptionStatus.REVOKED || next == AttemptExceptionStatus.EXPIRED
            AttemptExceptionStatus.REVOKED -> return next == AttemptExceptionStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AttemptExceptionStatus) {
        if (!canMoveTo(next)) {
            error("attempt exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt exception"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round((ceiling + priority.toDouble()) * 100) / 100

    fun snapshot(): AttemptException {
        return AttemptException(
            code = code,
            title = title,
            attributes = attributes,
            ceiling = ceiling,
            attempts = attempts,
            priority = priority,
            country = country,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a attempt exception in status requested. */
fun newAttemptException(code: String, title: String): AttemptException {
    return AttemptException(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        attempts = 0,
        priority = 0,
        country = "",
        timezone = "",
        status = AttemptExceptionStatus.REQUESTED,
    )
}
