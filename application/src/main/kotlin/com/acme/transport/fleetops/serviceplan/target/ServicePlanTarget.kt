package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan target records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan target is in its life cycle. */
enum class ServicePlanTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A service plan target as the service plan team keeps it. */
data class ServicePlanTarget(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var margin: Double,
    var discount: Double,
    var notes: String,
    var archived: Boolean,
    var name: String,
    var timezone: String,
    var status: ServicePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanTargetStatus): Boolean {
        when (status) {
            ServicePlanTargetStatus.OPEN -> return next == ServicePlanTargetStatus.ARCHIVED
            ServicePlanTargetStatus.IN_REVIEW -> return next == ServicePlanTargetStatus.REOPENED
            ServicePlanTargetStatus.RESOLVED -> return next == ServicePlanTargetStatus.ARCHIVED
            ServicePlanTargetStatus.REOPENED -> {
                when (next) {
                    ServicePlanTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("service plan target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service plan target"} $code"

    fun weightedExposure(): Double = round((discount - margin) * 100) / 100

    fun snapshot(): ServicePlanTarget {
        return ServicePlanTarget(
            code = code,
            label = label,
            pinned = pinned,
            margin = margin,
            discount = discount,
            notes = notes,
            archived = archived,
            name = name,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service plan target in status open. */
fun newServicePlanTarget(code: String, label: String): ServicePlanTarget {
    return ServicePlanTarget(
        code = code,
        label = label,
        pinned = false,
        margin = 0.0,
        discount = 0.0,
        notes = "",
        archived = false,
        name = "",
        timezone = "",
        status = ServicePlanTargetStatus.OPEN,
    )
}
