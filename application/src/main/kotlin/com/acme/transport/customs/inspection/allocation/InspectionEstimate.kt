package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection estimate is in its life cycle. */
enum class InspectionEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a inspection estimate and its current status. */
data class InspectionEstimate(
    var code: String,
    var title: String,
    var currency: String,
    var stopCount: Int,
    var createdAt: Int,
    var status: InspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionEstimateStatus): Boolean {
        when (status) {
            InspectionEstimateStatus.PLANNED -> return next == InspectionEstimateStatus.IN_PROGRESS || next == InspectionEstimateStatus.CANCELLED
            InspectionEstimateStatus.SCHEDULED -> return next == InspectionEstimateStatus.COMPLETED || next == InspectionEstimateStatus.CANCELLED
            InspectionEstimateStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionEstimateStatus.COMPLETED, InspectionEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionEstimateStatus.COMPLETED -> return next == InspectionEstimateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: InspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("inspection estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round(createdAt.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): InspectionEstimate {
        return InspectionEstimate(
            code = code,
            title = title,
            currency = currency,
            stopCount = stopCount,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection estimate in status planned. */
fun newInspectionEstimate(code: String, title: String): InspectionEstimate {
    return InspectionEstimate(
        code = code,
        title = title,
        currency = "",
        stopCount = 0,
        createdAt = 0,
        status = InspectionEstimateStatus.PLANNED,
    )
}
