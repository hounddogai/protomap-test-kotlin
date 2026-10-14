package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback rest break exception is in its life cycle. */
enum class FallbackRestBreakExceptionStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback rest break exception, with what the planners need to act on it. */
data class FallbackRestBreakException(
    var code: String,
    var name: String,
    var carrier: String,
    var quantity: Int,
    var ownerId: String,
    var tags: MutableList<String>,
    var startsAt: Int,
    var createdAt: Int,
    var attempts: Int,
    var approved: Boolean,
    var locale: String,
    var status: FallbackRestBreakExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRestBreakExceptionStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRestBreakExceptionStatus): Boolean {
        when (status) {
            FallbackRestBreakExceptionStatus.PROPOSED -> return next == FallbackRestBreakExceptionStatus.REJECTED
            FallbackRestBreakExceptionStatus.APPROVED -> return next == FallbackRestBreakExceptionStatus.WITHDRAWN
            FallbackRestBreakExceptionStatus.REJECTED -> {
                when (next) {
                    FallbackRestBreakExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRestBreakExceptionStatus) {
        if (!canMoveTo(next)) {
            error("fallback rest break exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback rest break exception"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round(attempts.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): FallbackRestBreakException {
        return FallbackRestBreakException(
            code = code,
            name = name,
            carrier = carrier,
            quantity = quantity,
            ownerId = ownerId,
            tags = tags,
            startsAt = startsAt,
            createdAt = createdAt,
            attempts = attempts,
            approved = approved,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback rest break exception in status proposed. */
fun newFallbackRestBreakException(code: String, name: String): FallbackRestBreakException {
    return FallbackRestBreakException(
        code = code,
        name = name,
        carrier = "",
        quantity = 0,
        ownerId = "",
        tags = mutableListOf<String>(),
        startsAt = 0,
        createdAt = 0,
        attempts = 0,
        approved = false,
        locale = "",
        status = FallbackRestBreakExceptionStatus.PROPOSED,
    )
}
