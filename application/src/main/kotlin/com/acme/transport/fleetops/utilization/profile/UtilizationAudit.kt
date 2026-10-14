package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization audit is in its life cycle. */
enum class UtilizationAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a utilization audit and its current status. */
data class UtilizationAudit(
    var code: String,
    var name: String,
    var pallets: Int,
    var ownerId: String,
    var description: String,
    var rate: Double,
    var externalRef: String,
    var margin: Double,
    var category: String,
    var driverId: String,
    var budget: Double,
    var status: UtilizationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationAuditStatus): Boolean {
        when (status) {
            UtilizationAuditStatus.PLANNED -> {
                when (next) {
                    UtilizationAuditStatus.SCHEDULED -> return true
                }
                return false
            }
            UtilizationAuditStatus.SCHEDULED -> {
                when (next) {
                    UtilizationAuditStatus.IN_PROGRESS, UtilizationAuditStatus.COMPLETED -> return true
                }
                return false
            }
            UtilizationAuditStatus.IN_PROGRESS -> return next == UtilizationAuditStatus.COMPLETED
            UtilizationAuditStatus.COMPLETED -> return next == UtilizationAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: UtilizationAuditStatus) {
        if (!canMoveTo(next)) {
            error("utilization audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"utilization audit"} $code"

    fun adjustedExposure(): Double = round((pallets.toDouble() - budget) * 100) / 100

    fun snapshot(): UtilizationAudit {
        return UtilizationAudit(
            code = code,
            name = name,
            pallets = pallets,
            ownerId = ownerId,
            description = description,
            rate = rate,
            externalRef = externalRef,
            margin = margin,
            category = category,
            driverId = driverId,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a utilization audit in status planned. */
fun newUtilizationAudit(code: String, name: String): UtilizationAudit {
    return UtilizationAudit(
        code = code,
        name = name,
        pallets = 0,
        ownerId = "",
        description = "",
        rate = 0.0,
        externalRef = "",
        margin = 0.0,
        category = "",
        driverId = "",
        budget = 0.0,
        status = UtilizationAuditStatus.PLANNED,
    )
}
