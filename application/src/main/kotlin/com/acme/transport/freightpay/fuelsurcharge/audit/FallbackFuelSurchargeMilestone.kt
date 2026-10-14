package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback fuel surcharge milestone is in its life cycle. */
enum class FallbackFuelSurchargeMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fallback fuel surcharge milestone and its current status. */
data class FallbackFuelSurchargeMilestone(
    var code: String,
    var title: String,
    var graceDays: Int,
    var routeCode: String,
    var threshold: Double,
    var phone: String,
    var unit: String,
    var distanceKm: Double,
    var ceiling: Double,
    var status: FallbackFuelSurchargeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFuelSurchargeMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFuelSurchargeMilestoneStatus): Boolean {
        when (status) {
            FallbackFuelSurchargeMilestoneStatus.OPEN -> {
                when (next) {
                    FallbackFuelSurchargeMilestoneStatus.RESOLVED -> return true
                }
                return false
            }
            FallbackFuelSurchargeMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    FallbackFuelSurchargeMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            FallbackFuelSurchargeMilestoneStatus.RESOLVED -> {
                when (next) {
                    FallbackFuelSurchargeMilestoneStatus.REOPENED, FallbackFuelSurchargeMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackFuelSurchargeMilestoneStatus.REOPENED -> return next == FallbackFuelSurchargeMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackFuelSurchargeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback fuel surcharge milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((distanceKm - ceiling) * 100) / 100

    fun snapshot(): FallbackFuelSurchargeMilestone {
        return FallbackFuelSurchargeMilestone(
            code = code,
            title = title,
            graceDays = graceDays,
            routeCode = routeCode,
            threshold = threshold,
            phone = phone,
            unit = unit,
            distanceKm = distanceKm,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback fuel surcharge milestone in status open. */
fun newFallbackFuelSurchargeMilestone(code: String, title: String): FallbackFuelSurchargeMilestone {
    return FallbackFuelSurchargeMilestone(
        code = code,
        title = title,
        graceDays = 0,
        routeCode = "",
        threshold = 0.0,
        phone = "",
        unit = "",
        distanceKm = 0.0,
        ceiling = 0.0,
        status = FallbackFuelSurchargeMilestoneStatus.OPEN,
    )
}
