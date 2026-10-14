package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive inspection rule records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a adaptive inspection rule is in its life cycle. */
enum class AdaptiveInspectionRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive inspection rule. Its code is unique within a tenant. */
data class AdaptiveInspectionRule(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var carrier: String,
    var windowDays: Int,
    var score: Double,
    var loadKg: Double,
    var odometer: Int,
    var vehicleId: String,
    var status: AdaptiveInspectionRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionRuleStatus): Boolean {
        when (status) {
            AdaptiveInspectionRuleStatus.PROPOSED -> return next == AdaptiveInspectionRuleStatus.REJECTED
            AdaptiveInspectionRuleStatus.APPROVED -> {
                when (next) {
                    AdaptiveInspectionRuleStatus.REJECTED, AdaptiveInspectionRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveInspectionRuleStatus.REJECTED -> {
                when (next) {
                    AdaptiveInspectionRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionRuleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive inspection rule"} $code"

    fun projectedLoad(): Double = round((loadKg + score) * 100) / 100

    fun snapshot(): AdaptiveInspectionRule {
        return AdaptiveInspectionRule(
            code = code,
            label = label,
            pinned = pinned,
            carrier = carrier,
            windowDays = windowDays,
            score = score,
            loadKg = loadKg,
            odometer = odometer,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive inspection rule in status proposed. */
fun newAdaptiveInspectionRule(code: String, label: String): AdaptiveInspectionRule {
    return AdaptiveInspectionRule(
        code = code,
        label = label,
        pinned = false,
        carrier = "",
        windowDays = 0,
        score = 0.0,
        loadKg = 0.0,
        odometer = 0,
        vehicleId = "",
        status = AdaptiveInspectionRuleStatus.PROPOSED,
    )
}
