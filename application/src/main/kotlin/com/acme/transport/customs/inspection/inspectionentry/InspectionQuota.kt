package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection quota is in its life cycle. */
enum class InspectionQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a inspection quota and its current status. */
data class InspectionQuota(
    var code: String,
    var label: String,
    var priority: Int,
    var trailerId: String,
    var shiftCode: String,
    var depot: String,
    var archived: Boolean,
    var driverId: String,
    var revision: Int,
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
            InspectionQuotaStatus.OPEN -> {
                when (next) {
                    InspectionQuotaStatus.IN_REVIEW, InspectionQuotaStatus.RESOLVED -> return true
                }
                return false
            }
            InspectionQuotaStatus.IN_REVIEW -> {
                when (next) {
                    InspectionQuotaStatus.REOPENED -> return true
                }
                return false
            }
            InspectionQuotaStatus.RESOLVED -> return next == InspectionQuotaStatus.REOPENED
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
        revision += 1
    }

    fun displayLabel(): String = "${"inspection quota"} $code"

    fun adjustedExposure(): Double = round((priority.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): InspectionQuota {
        return InspectionQuota(
            code = code,
            label = label,
            priority = priority,
            trailerId = trailerId,
            shiftCode = shiftCode,
            depot = depot,
            archived = archived,
            driverId = driverId,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a inspection quota in status open. */
fun newInspectionQuota(code: String, label: String): InspectionQuota {
    return InspectionQuota(
        code = code,
        label = label,
        priority = 0,
        trailerId = "",
        shiftCode = "",
        depot = "",
        archived = false,
        driverId = "",
        revision = 0,
        status = InspectionQuotaStatus.OPEN,
    )
}
