package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit plan records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit plan is in its life cycle. */
enum class AuditPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A audit plan as the audit team keeps it. */
data class AuditPlan(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var updatedAt: Int,
    var vehicleId: String,
    var capacity: Int,
    var visible: Boolean,
    var status: AuditPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditPlanStatus): Boolean {
        when (status) {
            AuditPlanStatus.PLANNED -> {
                when (next) {
                    AuditPlanStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AuditPlanStatus.SCHEDULED -> {
                when (next) {
                    AuditPlanStatus.COMPLETED -> return true
                }
                return false
            }
            AuditPlanStatus.IN_PROGRESS -> return next == AuditPlanStatus.COMPLETED || next == AuditPlanStatus.CANCELLED
            AuditPlanStatus.COMPLETED -> return next == AuditPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AuditPlanStatus) {
        if (!canMoveTo(next)) {
            error("audit plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((durationMinutes.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): AuditPlan {
        return AuditPlan(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            capacity = capacity,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a audit plan in status planned. */
fun newAuditPlan(code: String, name: String): AuditPlan {
    return AuditPlan(
        code = code,
        name = name,
        durationMinutes = 0,
        updatedAt = 0,
        vehicleId = "",
        capacity = 0,
        visible = false,
        status = AuditPlanStatus.PLANNED,
    )
}
