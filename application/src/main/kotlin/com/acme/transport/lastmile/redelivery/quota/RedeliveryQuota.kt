package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery quota records of lastmile's redelivery work, and the rules, stores, and calculations around
// them.

/** Where a redelivery quota is in its life cycle. */
enum class RedeliveryQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One redelivery quota, with what the planners need to act on it. */
data class RedeliveryQuota(
    var code: String,
    var label: String,
    var cost: Double,
    var graceDays: Int,
    var amount: Double,
    var status: RedeliveryQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryQuotaStatus): Boolean {
        when (status) {
            RedeliveryQuotaStatus.PROPOSED -> return next == RedeliveryQuotaStatus.APPROVED || next == RedeliveryQuotaStatus.REJECTED
            RedeliveryQuotaStatus.APPROVED -> return next == RedeliveryQuotaStatus.REJECTED || next == RedeliveryQuotaStatus.WITHDRAWN
            RedeliveryQuotaStatus.REJECTED -> {
                when (next) {
                    RedeliveryQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryQuotaStatus) {
        if (!canMoveTo(next)) {
            error("redelivery quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netTotal(): Double = round((graceDays.toDouble() + cost) * 100) / 100

    fun snapshot(): RedeliveryQuota {
        return RedeliveryQuota(
            code = code,
            label = label,
            cost = cost,
            graceDays = graceDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a redelivery quota in status proposed. */
fun newRedeliveryQuota(code: String, label: String): RedeliveryQuota {
    return RedeliveryQuota(
        code = code,
        label = label,
        cost = 0.0,
        graceDays = 0,
        amount = 0.0,
        status = RedeliveryQuotaStatus.PROPOSED,
    )
}
