package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.text.normalize

/** Where a default utilization plan is in its life cycle. */
enum class DefaultUtilizationPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default utilization plan. Its code is unique within a tenant. */
data class DefaultUtilizationPlan(
    var code: String,
    var name: String,
    var carrier: String,
    var notes: String,
    var ownerId: String,
    var score: Double,
    var status: DefaultUtilizationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultUtilizationPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultUtilizationPlanStatus): Boolean {
        when (status) {
            DefaultUtilizationPlanStatus.CREATED -> {
                when (next) {
                    DefaultUtilizationPlanStatus.VALIDATED -> return true
                }
                return false
            }
            DefaultUtilizationPlanStatus.VALIDATED -> return next == DefaultUtilizationPlanStatus.PUBLISHED || next == DefaultUtilizationPlanStatus.RETIRED
            DefaultUtilizationPlanStatus.PUBLISHED -> {
                when (next) {
                    DefaultUtilizationPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultUtilizationPlanStatus) {
        if (!canMoveTo(next)) {
            error("default utilization plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default utilization plan"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): DefaultUtilizationPlan {
        return DefaultUtilizationPlan(
            code = code,
            name = name,
            carrier = carrier,
            notes = notes,
            ownerId = ownerId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default utilization plan in status created. */
fun newDefaultUtilizationPlan(code: String, name: String): DefaultUtilizationPlan {
    return DefaultUtilizationPlan(
        code = code,
        name = name,
        carrier = "",
        notes = "",
        ownerId = "",
        score = 0.0,
        status = DefaultUtilizationPlanStatus.CREATED,
    )
}
