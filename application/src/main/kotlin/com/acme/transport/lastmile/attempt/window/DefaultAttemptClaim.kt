package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default attempt claim is in its life cycle. */
enum class DefaultAttemptClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a default attempt claim and its current status. */
data class DefaultAttemptClaim(
    var code: String,
    var title: String,
    var limit: Int,
    var quantity: Int,
    var graceDays: Int,
    var segment: String,
    var country: String,
    var status: DefaultAttemptClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAttemptClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAttemptClaimStatus): Boolean {
        when (status) {
            DefaultAttemptClaimStatus.DRAFT -> {
                when (next) {
                    DefaultAttemptClaimStatus.ACTIVE, DefaultAttemptClaimStatus.CLOSED -> return true
                }
                return false
            }
            DefaultAttemptClaimStatus.PENDING -> {
                when (next) {
                    DefaultAttemptClaimStatus.ACTIVE, DefaultAttemptClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultAttemptClaimStatus.ACTIVE -> {
                when (next) {
                    DefaultAttemptClaimStatus.SUSPENDED, DefaultAttemptClaimStatus.CLOSED -> return true
                }
                return false
            }
            DefaultAttemptClaimStatus.SUSPENDED -> return next == DefaultAttemptClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultAttemptClaimStatus) {
        if (!canMoveTo(next)) {
            error("default attempt claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round((limit.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): DefaultAttemptClaim {
        return DefaultAttemptClaim(
            code = code,
            title = title,
            limit = limit,
            quantity = quantity,
            graceDays = graceDays,
            segment = segment,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default attempt claim in status draft. */
fun newDefaultAttemptClaim(code: String, title: String): DefaultAttemptClaim {
    return DefaultAttemptClaim(
        code = code,
        title = title,
        limit = 0,
        quantity = 0,
        graceDays = 0,
        segment = "",
        country = "",
        status = DefaultAttemptClaimStatus.DRAFT,
    )
}
