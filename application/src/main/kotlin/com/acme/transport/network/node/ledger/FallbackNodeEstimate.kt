package com.acme.transport.network.node.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback node estimate is in its life cycle. */
enum class FallbackNodeEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback node estimate. Its code is unique within a tenant. */
data class FallbackNodeEstimate(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var durationMinutes: Int,
    var contactEmail: String,
    var status: FallbackNodeEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackNodeEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackNodeEstimateStatus): Boolean {
        when (status) {
            FallbackNodeEstimateStatus.CREATED -> {
                when (next) {
                    FallbackNodeEstimateStatus.PUBLISHED, FallbackNodeEstimateStatus.RETIRED -> return true
                }
                return false
            }
            FallbackNodeEstimateStatus.VALIDATED -> return next == FallbackNodeEstimateStatus.RETIRED
            FallbackNodeEstimateStatus.PUBLISHED -> return next == FallbackNodeEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackNodeEstimateStatus) {
        if (!canMoveTo(next)) {
            error("fallback node estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback node estimate"} $code"

    fun projectedLoad(): Double = round(durationMinutes.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): FallbackNodeEstimate {
        return FallbackNodeEstimate(
            code = code,
            name = name,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback node estimate in status created. */
fun newFallbackNodeEstimate(code: String, name: String): FallbackNodeEstimate {
    return FallbackNodeEstimate(
        code = code,
        name = name,
        expiresAt = 0,
        durationMinutes = 0,
        contactEmail = "",
        status = FallbackNodeEstimateStatus.CREATED,
    )
}
