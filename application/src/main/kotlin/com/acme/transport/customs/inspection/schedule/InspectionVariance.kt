package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection variance is in its life cycle. */
enum class InspectionVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A inspection variance. Its code is unique within a tenant. */
data class InspectionVariance(
    var code: String,
    var name: String,
    var locale: String,
    var cost: Double,
    var etaMinutes: Int,
    var status: InspectionVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionVarianceStatus): Boolean {
        when (status) {
            InspectionVarianceStatus.PLANNED -> return next == InspectionVarianceStatus.SCHEDULED || next == InspectionVarianceStatus.CANCELLED
            InspectionVarianceStatus.SCHEDULED -> {
                when (next) {
                    InspectionVarianceStatus.IN_PROGRESS, InspectionVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionVarianceStatus.COMPLETED, InspectionVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionVarianceStatus.COMPLETED -> {
                when (next) {
                    InspectionVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionVarianceStatus) {
        if (!canMoveTo(next)) {
            error("inspection variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection variance"} $code"

    fun weightedVolume(): Double = round((cost + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): InspectionVariance {
        return InspectionVariance(
            code = code,
            name = name,
            locale = locale,
            cost = cost,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a inspection variance in status planned. */
fun newInspectionVariance(code: String, name: String): InspectionVariance {
    return InspectionVariance(
        code = code,
        name = name,
        locale = "",
        cost = 0.0,
        etaMinutes = 0,
        status = InspectionVarianceStatus.PLANNED,
    )
}
