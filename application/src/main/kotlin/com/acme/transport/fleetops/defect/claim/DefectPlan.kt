package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect plan is in its life cycle. */
enum class DefectPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A defect plan. Its code is unique within a tenant. */
data class DefectPlan(
    var code: String,
    var name: String,
    var endsAt: Int,
    var routeCode: String,
    var title: String,
    var distanceKm: Double,
    var status: DefectPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectPlanStatus): Boolean {
        when (status) {
            DefectPlanStatus.PROPOSED -> {
                when (next) {
                    DefectPlanStatus.REJECTED -> return true
                }
                return false
            }
            DefectPlanStatus.APPROVED -> return next == DefectPlanStatus.REJECTED || next == DefectPlanStatus.WITHDRAWN
            DefectPlanStatus.REJECTED -> {
                when (next) {
                    DefectPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectPlanStatus) {
        if (!canMoveTo(next)) {
            error("defect plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round(endsAt.toDouble() * distanceKm * 100) / 100

    fun snapshot(): DefectPlan {
        return DefectPlan(
            code = code,
            name = name,
            endsAt = endsAt,
            routeCode = routeCode,
            title = title,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a defect plan in status proposed. */
fun newDefectPlan(code: String, name: String): DefectPlan {
    return DefectPlan(
        code = code,
        name = name,
        endsAt = 0,
        routeCode = "",
        title = "",
        distanceKm = 0.0,
        status = DefectPlanStatus.PROPOSED,
    )
}
