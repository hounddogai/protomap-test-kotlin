package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.text.normalize

/** Where a redelivery plan is in its life cycle. */
enum class RedeliveryPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A redelivery plan. Its code is unique within a tenant. */
data class RedeliveryPlan(
    var code: String,
    var label: String,
    var summary: String,
    var carrier: String,
    var margin: Double,
    var status: RedeliveryPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryPlanStatus): Boolean {
        when (status) {
            RedeliveryPlanStatus.REQUESTED -> {
                when (next) {
                    RedeliveryPlanStatus.EXPIRED -> return true
                }
                return false
            }
            RedeliveryPlanStatus.GRANTED -> {
                when (next) {
                    RedeliveryPlanStatus.REVOKED -> return true
                }
                return false
            }
            RedeliveryPlanStatus.REVOKED -> return next == RedeliveryPlanStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RedeliveryPlanStatus) {
        if (!canMoveTo(next)) {
            error("redelivery plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"redelivery plan"} $code"

    fun measuredValue(): Double = margin

    fun snapshot(): RedeliveryPlan {
        return RedeliveryPlan(
            code = code,
            label = label,
            summary = summary,
            carrier = carrier,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a redelivery plan in status requested. */
fun newRedeliveryPlan(code: String, label: String): RedeliveryPlan {
    return RedeliveryPlan(
        code = code,
        label = label,
        summary = "",
        carrier = "",
        margin = 0.0,
        status = RedeliveryPlanStatus.REQUESTED,
    )
}
