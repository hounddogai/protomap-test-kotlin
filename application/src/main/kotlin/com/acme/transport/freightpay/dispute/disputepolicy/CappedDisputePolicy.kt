package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped dispute policy records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a capped dispute policy is in its life cycle. */
enum class CappedDisputePolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capped dispute policy and its current status. */
data class CappedDisputePolicy(
    var code: String,
    var title: String,
    var graceDays: Int,
    var limit: Int,
    var locale: String,
    var ownerId: String,
    var shiftCode: String,
    var status: CappedDisputePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDisputePolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDisputePolicyStatus): Boolean {
        when (status) {
            CappedDisputePolicyStatus.PLANNED -> return next == CappedDisputePolicyStatus.CANCELLED
            CappedDisputePolicyStatus.SCHEDULED -> return next == CappedDisputePolicyStatus.CANCELLED
            CappedDisputePolicyStatus.IN_PROGRESS -> return next == CappedDisputePolicyStatus.CANCELLED
            CappedDisputePolicyStatus.COMPLETED -> {
                when (next) {
                    CappedDisputePolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDisputePolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped dispute policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netExposure(): Double = round((graceDays.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): CappedDisputePolicy {
        return CappedDisputePolicy(
            code = code,
            title = title,
            graceDays = graceDays,
            limit = limit,
            locale = locale,
            ownerId = ownerId,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped dispute policy in status planned. */
fun newCappedDisputePolicy(code: String, title: String): CappedDisputePolicy {
    return CappedDisputePolicy(
        code = code,
        title = title,
        graceDays = 0,
        limit = 0,
        locale = "",
        ownerId = "",
        shiftCode = "",
        status = CappedDisputePolicyStatus.PLANNED,
    )
}
