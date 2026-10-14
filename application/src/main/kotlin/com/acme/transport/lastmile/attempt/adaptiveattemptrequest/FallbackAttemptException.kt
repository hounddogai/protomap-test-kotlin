package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback attempt exception is in its life cycle. */
enum class FallbackAttemptExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fallback attempt exception, with what the planners need to act on it. */
data class FallbackAttemptException(
    var code: String,
    var label: String,
    var priority: Int,
    var version: Int,
    var score: Double,
    var notes: String,
    var capacity: Int,
    var distanceKm: Double,
    var etaMinutes: Int,
    var expiresAt: Int,
    var timezone: String,
    var status: FallbackAttemptExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAttemptExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAttemptExceptionStatus): Boolean {
        when (status) {
            FallbackAttemptExceptionStatus.REQUESTED -> {
                when (next) {
                    FallbackAttemptExceptionStatus.GRANTED, FallbackAttemptExceptionStatus.REVOKED -> return true
                }
                return false
            }
            FallbackAttemptExceptionStatus.GRANTED -> return next == FallbackAttemptExceptionStatus.REVOKED || next == FallbackAttemptExceptionStatus.EXPIRED
            FallbackAttemptExceptionStatus.REVOKED -> return next == FallbackAttemptExceptionStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackAttemptExceptionStatus) {
        if (!canMoveTo(next)) {
            error("fallback attempt exception $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round(priority.toDouble() * distanceKm * 100) / 100

    fun snapshot(): FallbackAttemptException {
        return FallbackAttemptException(
            code = code,
            label = label,
            priority = priority,
            version = version,
            score = score,
            notes = notes,
            capacity = capacity,
            distanceKm = distanceKm,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback attempt exception in status requested. */
fun newFallbackAttemptException(code: String, label: String): FallbackAttemptException {
    return FallbackAttemptException(
        code = code,
        label = label,
        priority = 0,
        version = 0,
        score = 0.0,
        notes = "",
        capacity = 0,
        distanceKm = 0.0,
        etaMinutes = 0,
        expiresAt = 0,
        timezone = "",
        status = FallbackAttemptExceptionStatus.REQUESTED,
    )
}
