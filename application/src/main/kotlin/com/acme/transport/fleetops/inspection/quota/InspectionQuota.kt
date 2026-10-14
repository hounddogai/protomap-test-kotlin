package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection quota records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection quota is in its life cycle. */
enum class InspectionQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One inspection quota, with what the planners need to act on it. */
data class InspectionQuota(
    var code: String,
    var label: String,
    var title: String,
    var routeCode: String,
    var version: Int,
    var weight: Double,
    var status: InspectionQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionQuotaStatus): Boolean {
        when (status) {
            InspectionQuotaStatus.OPEN -> return next == InspectionQuotaStatus.IN_REVIEW
            InspectionQuotaStatus.IN_REVIEW -> return next == InspectionQuotaStatus.RESOLVED || next == InspectionQuotaStatus.REOPENED
            InspectionQuotaStatus.RESOLVED -> return next == InspectionQuotaStatus.ARCHIVED
            InspectionQuotaStatus.REOPENED -> {
                when (next) {
                    InspectionQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionQuotaStatus) {
        if (!canMoveTo(next)) {
            error("inspection quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round(version.toDouble() * weight * 100) / 100

    fun snapshot(): InspectionQuota {
        return InspectionQuota(
            code = code,
            label = label,
            title = title,
            routeCode = routeCode,
            version = version,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection quota in status open. */
fun newInspectionQuota(code: String, label: String): InspectionQuota {
    return InspectionQuota(
        code = code,
        label = label,
        title = "",
        routeCode = "",
        version = 0,
        weight = 0.0,
        status = InspectionQuotaStatus.OPEN,
    )
}
