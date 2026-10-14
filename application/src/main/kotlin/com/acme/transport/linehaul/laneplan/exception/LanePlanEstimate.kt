package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.normalize

/** Where a lane plan estimate is in its life cycle. */
enum class LanePlanEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lane plan estimate. Its code is unique within a tenant. */
data class LanePlanEstimate(
    var code: String,
    var label: String,
    var phone: String,
    var description: String,
    var limit: Int,
    var routeCode: String,
    var status: LanePlanEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanEstimateStatus): Boolean {
        when (status) {
            LanePlanEstimateStatus.PLANNED -> return next == LanePlanEstimateStatus.COMPLETED
            LanePlanEstimateStatus.SCHEDULED -> return next == LanePlanEstimateStatus.COMPLETED || next == LanePlanEstimateStatus.CANCELLED
            LanePlanEstimateStatus.IN_PROGRESS -> {
                when (next) {
                    LanePlanEstimateStatus.COMPLETED -> return true
                }
                return false
            }
            LanePlanEstimateStatus.COMPLETED -> {
                when (next) {
                    LanePlanEstimateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lane plan estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lane plan estimate"} $code"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): LanePlanEstimate {
        return LanePlanEstimate(
            code = code,
            label = label,
            phone = phone,
            description = description,
            limit = limit,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lane plan estimate in status planned. */
fun newLanePlanEstimate(code: String, label: String): LanePlanEstimate {
    return LanePlanEstimate(
        code = code,
        label = label,
        phone = "",
        description = "",
        limit = 0,
        routeCode = "",
        status = LanePlanEstimateStatus.PLANNED,
    )
}
