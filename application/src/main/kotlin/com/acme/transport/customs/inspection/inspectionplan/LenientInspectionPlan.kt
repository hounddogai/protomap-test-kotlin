package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient inspection plan records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a lenient inspection plan is in its life cycle. */
enum class LenientInspectionPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lenient inspection plan, with what the planners need to act on it. */
data class LenientInspectionPlan(
    var code: String,
    var title: String,
    var country: String,
    var carrier: String,
    var etaMinutes: Int,
    var currency: String,
    var source: String,
    var margin: Double,
    var active: Boolean,
    var status: LenientInspectionPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientInspectionPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientInspectionPlanStatus): Boolean {
        when (status) {
            LenientInspectionPlanStatus.IDLE -> {
                when (next) {
                    LenientInspectionPlanStatus.WARMING -> return true
                }
                return false
            }
            LenientInspectionPlanStatus.WARMING -> {
                when (next) {
                    LenientInspectionPlanStatus.READY -> return true
                }
                return false
            }
            LenientInspectionPlanStatus.READY -> {
                when (next) {
                    LenientInspectionPlanStatus.DRAINING, LenientInspectionPlanStatus.STOPPED -> return true
                }
                return false
            }
            LenientInspectionPlanStatus.DRAINING -> {
                when (next) {
                    LenientInspectionPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientInspectionPlanStatus) {
        if (!canMoveTo(next)) {
            error("lenient inspection plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((etaMinutes.toDouble() + margin) * 100) / 100

    fun snapshot(): LenientInspectionPlan {
        return LenientInspectionPlan(
            code = code,
            title = title,
            country = country,
            carrier = carrier,
            etaMinutes = etaMinutes,
            currency = currency,
            source = source,
            margin = margin,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient inspection plan in status idle. */
fun newLenientInspectionPlan(code: String, title: String): LenientInspectionPlan {
    return LenientInspectionPlan(
        code = code,
        title = title,
        country = "",
        carrier = "",
        etaMinutes = 0,
        currency = "",
        source = "",
        margin = 0.0,
        active = false,
        status = LenientInspectionPlanStatus.IDLE,
    )
}
