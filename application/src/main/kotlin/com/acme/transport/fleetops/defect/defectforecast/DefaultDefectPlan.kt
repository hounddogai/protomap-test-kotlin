package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default defect plan is in its life cycle. */
enum class DefaultDefectPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default defect plan and its current status. */
data class DefaultDefectPlan(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var pinned: Boolean,
    var threshold: Double,
    var weight: Double,
    var status: DefaultDefectPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDefectPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDefectPlanStatus): Boolean {
        when (status) {
            DefaultDefectPlanStatus.REQUESTED -> return next == DefaultDefectPlanStatus.REVOKED || next == DefaultDefectPlanStatus.EXPIRED
            DefaultDefectPlanStatus.GRANTED -> {
                when (next) {
                    DefaultDefectPlanStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultDefectPlanStatus.REVOKED -> return next == DefaultDefectPlanStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultDefectPlanStatus) {
        if (!canMoveTo(next)) {
            error("default defect plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round((threshold - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefaultDefectPlan {
        return DefaultDefectPlan(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            pinned = pinned,
            threshold = threshold,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default defect plan in status requested. */
fun newDefaultDefectPlan(code: String, title: String): DefaultDefectPlan {
    return DefaultDefectPlan(
        code = code,
        title = title,
        etaMinutes = 0,
        pinned = false,
        threshold = 0.0,
        weight = 0.0,
        status = DefaultDefectPlanStatus.REQUESTED,
    )
}
