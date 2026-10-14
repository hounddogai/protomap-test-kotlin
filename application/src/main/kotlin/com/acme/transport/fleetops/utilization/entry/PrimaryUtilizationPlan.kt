package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.text.normalize

/** Where a primary utilization plan is in its life cycle. */
enum class PrimaryUtilizationPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary utilization plan. Its code is unique within a tenant. */
data class PrimaryUtilizationPlan(
    var code: String,
    var label: String,
    var contactEmail: String,
    var sequence: Int,
    var approved: Boolean,
    var status: PrimaryUtilizationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryUtilizationPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryUtilizationPlanStatus): Boolean {
        when (status) {
            PrimaryUtilizationPlanStatus.IDLE -> {
                when (next) {
                    PrimaryUtilizationPlanStatus.DRAINING, PrimaryUtilizationPlanStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryUtilizationPlanStatus.WARMING -> {
                when (next) {
                    PrimaryUtilizationPlanStatus.READY, PrimaryUtilizationPlanStatus.DRAINING -> return true
                }
                return false
            }
            PrimaryUtilizationPlanStatus.READY -> {
                when (next) {
                    PrimaryUtilizationPlanStatus.DRAINING, PrimaryUtilizationPlanStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryUtilizationPlanStatus.DRAINING -> {
                when (next) {
                    PrimaryUtilizationPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryUtilizationPlanStatus) {
        if (!canMoveTo(next)) {
            error("primary utilization plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary utilization plan"} $code"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): PrimaryUtilizationPlan {
        return PrimaryUtilizationPlan(
            code = code,
            label = label,
            contactEmail = contactEmail,
            sequence = sequence,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary utilization plan in status idle. */
fun newPrimaryUtilizationPlan(code: String, label: String): PrimaryUtilizationPlan {
    return PrimaryUtilizationPlan(
        code = code,
        label = label,
        contactEmail = "",
        sequence = 0,
        approved = false,
        status = PrimaryUtilizationPlanStatus.IDLE,
    )
}
