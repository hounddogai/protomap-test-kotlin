package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.kit.text.normalize

/** Where a fallback dispute tier is in its life cycle. */
enum class FallbackDisputeTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a fallback dispute tier and its current status. */
data class FallbackDisputeTier(
    var code: String,
    var title: String,
    var createdAt: Int,
    var shiftCode: String,
    var locale: String,
    var pinned: Boolean,
    var carrier: String,
    var trailerId: String,
    var status: FallbackDisputeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDisputeTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDisputeTierStatus): Boolean {
        when (status) {
            FallbackDisputeTierStatus.PROPOSED -> return next == FallbackDisputeTierStatus.WITHDRAWN
            FallbackDisputeTierStatus.APPROVED -> {
                when (next) {
                    FallbackDisputeTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackDisputeTierStatus.REJECTED -> return next == FallbackDisputeTierStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackDisputeTierStatus) {
        if (!canMoveTo(next)) {
            error("fallback dispute tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback dispute tier"} $code"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): FallbackDisputeTier {
        return FallbackDisputeTier(
            code = code,
            title = title,
            createdAt = createdAt,
            shiftCode = shiftCode,
            locale = locale,
            pinned = pinned,
            carrier = carrier,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback dispute tier in status proposed. */
fun newFallbackDisputeTier(code: String, title: String): FallbackDisputeTier {
    return FallbackDisputeTier(
        code = code,
        title = title,
        createdAt = 0,
        shiftCode = "",
        locale = "",
        pinned = false,
        carrier = "",
        trailerId = "",
        status = FallbackDisputeTierStatus.PROPOSED,
    )
}
