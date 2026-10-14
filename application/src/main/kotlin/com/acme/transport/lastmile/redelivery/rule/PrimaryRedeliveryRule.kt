package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary redelivery rule records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a primary redelivery rule is in its life cycle. */
enum class PrimaryRedeliveryRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary redelivery rule. Its code is unique within a tenant. */
data class PrimaryRedeliveryRule(
    var code: String,
    var name: String,
    var floorValue: Double,
    var routeCode: String,
    var stopCount: Int,
    var timezone: String,
    var status: PrimaryRedeliveryRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRedeliveryRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRedeliveryRuleStatus): Boolean {
        when (status) {
            PrimaryRedeliveryRuleStatus.IDLE -> return next == PrimaryRedeliveryRuleStatus.WARMING || next == PrimaryRedeliveryRuleStatus.READY
            PrimaryRedeliveryRuleStatus.WARMING -> {
                when (next) {
                    PrimaryRedeliveryRuleStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryRedeliveryRuleStatus.READY -> return next == PrimaryRedeliveryRuleStatus.DRAINING
            PrimaryRedeliveryRuleStatus.DRAINING -> return next == PrimaryRedeliveryRuleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryRedeliveryRuleStatus) {
        if (!canMoveTo(next)) {
            error("primary redelivery rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary redelivery rule"} $code"

    fun netValue(): Double = round((floorValue + stopCount.toDouble()) * 100) / 100

    fun snapshot(): PrimaryRedeliveryRule {
        return PrimaryRedeliveryRule(
            code = code,
            name = name,
            floorValue = floorValue,
            routeCode = routeCode,
            stopCount = stopCount,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary redelivery rule in status idle. */
fun newPrimaryRedeliveryRule(code: String, name: String): PrimaryRedeliveryRule {
    return PrimaryRedeliveryRule(
        code = code,
        name = name,
        floorValue = 0.0,
        routeCode = "",
        stopCount = 0,
        timezone = "",
        status = PrimaryRedeliveryRuleStatus.IDLE,
    )
}
