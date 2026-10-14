package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient inspection estimate records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a lenient inspection estimate is in its life cycle. */
enum class LenientInspectionEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lenient inspection estimate and its current status. */
data class LenientInspectionEstimate(
    var code: String,
    var title: String,
    var locale: String,
    var category: String,
    var weight: Double,
    var ceiling: Double,
    var notes: String,
    var startsAt: Int,
    var name: String,
    var capacity: Int,
    var routeCode: String,
    var status: LenientInspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientInspectionEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientInspectionEstimateStatus): Boolean {
        when (status) {
            LenientInspectionEstimateStatus.PROPOSED -> return next == LenientInspectionEstimateStatus.APPROVED || next == LenientInspectionEstimateStatus.WITHDRAWN
            LenientInspectionEstimateStatus.APPROVED -> return next == LenientInspectionEstimateStatus.REJECTED || next == LenientInspectionEstimateStatus.WITHDRAWN
            LenientInspectionEstimateStatus.REJECTED -> {
                when (next) {
                    LenientInspectionEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientInspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lenient inspection estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient inspection estimate"} $code"

    fun weightedExposure(): Double = round(startsAt.toDouble() * weight * 100) / 100

    fun snapshot(): LenientInspectionEstimate {
        return LenientInspectionEstimate(
            code = code,
            title = title,
            locale = locale,
            category = category,
            weight = weight,
            ceiling = ceiling,
            notes = notes,
            startsAt = startsAt,
            name = name,
            capacity = capacity,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient inspection estimate in status proposed. */
fun newLenientInspectionEstimate(code: String, title: String): LenientInspectionEstimate {
    return LenientInspectionEstimate(
        code = code,
        title = title,
        locale = "",
        category = "",
        weight = 0.0,
        ceiling = 0.0,
        notes = "",
        startsAt = 0,
        name = "",
        capacity = 0,
        routeCode = "",
        status = LenientInspectionEstimateStatus.PROPOSED,
    )
}
