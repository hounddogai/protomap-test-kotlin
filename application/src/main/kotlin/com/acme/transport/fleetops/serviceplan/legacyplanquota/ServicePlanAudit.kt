package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan audit is in its life cycle. */
enum class ServicePlanAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A service plan audit as the service plan team keeps it. */
data class ServicePlanAudit(
    var code: String,
    var name: String,
    var threshold: Double,
    var shiftCode: String,
    var tags: MutableList<String>,
    var budget: Double,
    var floorValue: Double,
    var status: ServicePlanAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanAuditStatus): Boolean {
        when (status) {
            ServicePlanAuditStatus.PLANNED -> {
                when (next) {
                    ServicePlanAuditStatus.CANCELLED -> return true
                }
                return false
            }
            ServicePlanAuditStatus.SCHEDULED -> return next == ServicePlanAuditStatus.COMPLETED || next == ServicePlanAuditStatus.CANCELLED
            ServicePlanAuditStatus.IN_PROGRESS -> {
                when (next) {
                    ServicePlanAuditStatus.COMPLETED, ServicePlanAuditStatus.CANCELLED -> return true
                }
                return false
            }
            ServicePlanAuditStatus.COMPLETED -> return next == ServicePlanAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ServicePlanAuditStatus) {
        if (!canMoveTo(next)) {
            error("service plan audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((threshold + floorValue) * 100) / 100

    fun snapshot(): ServicePlanAudit {
        return ServicePlanAudit(
            code = code,
            name = name,
            threshold = threshold,
            shiftCode = shiftCode,
            tags = tags,
            budget = budget,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan audit in status planned. */
fun newServicePlanAudit(code: String, name: String): ServicePlanAudit {
    return ServicePlanAudit(
        code = code,
        name = name,
        threshold = 0.0,
        shiftCode = "",
        tags = mutableListOf<String>(),
        budget = 0.0,
        floorValue = 0.0,
        status = ServicePlanAuditStatus.PLANNED,
    )
}
