package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient drop rule is in its life cycle. */
enum class LenientDropRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient drop rule and its current status. */
data class LenientDropRule(
    var code: String,
    var label: String,
    var maximum: Double,
    var batchSize: Int,
    var depot: String,
    var weight: Double,
    var active: Boolean,
    var summary: String,
    var unit: String,
    var floorValue: Double,
    var stopCount: Int,
    var status: LenientDropRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDropRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDropRuleStatus): Boolean {
        when (status) {
            LenientDropRuleStatus.IDLE -> return next == LenientDropRuleStatus.WARMING || next == LenientDropRuleStatus.DRAINING
            LenientDropRuleStatus.WARMING -> return next == LenientDropRuleStatus.STOPPED
            LenientDropRuleStatus.READY -> return next == LenientDropRuleStatus.STOPPED
            LenientDropRuleStatus.DRAINING -> return next == LenientDropRuleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientDropRuleStatus) {
        if (!canMoveTo(next)) {
            error("lenient drop rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient drop rule"} $code"

    fun projectedValue(): Double = round(weight * maximum * 100) / 100

    fun snapshot(): LenientDropRule {
        return LenientDropRule(
            code = code,
            label = label,
            maximum = maximum,
            batchSize = batchSize,
            depot = depot,
            weight = weight,
            active = active,
            summary = summary,
            unit = unit,
            floorValue = floorValue,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient drop rule in status idle. */
fun newLenientDropRule(code: String, label: String): LenientDropRule {
    return LenientDropRule(
        code = code,
        label = label,
        maximum = 0.0,
        batchSize = 0,
        depot = "",
        weight = 0.0,
        active = false,
        summary = "",
        unit = "",
        floorValue = 0.0,
        stopCount = 0,
        status = LenientDropRuleStatus.IDLE,
    )
}
