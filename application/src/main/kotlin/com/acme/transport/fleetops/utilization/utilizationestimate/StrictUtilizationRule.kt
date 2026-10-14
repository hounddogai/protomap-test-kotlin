package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict utilization rule is in its life cycle. */
enum class StrictUtilizationRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict utilization rule as the utilization team keeps it. */
data class StrictUtilizationRule(
    var code: String,
    var title: String,
    var budget: Double,
    var priority: Int,
    var floorValue: Double,
    var version: Int,
    var status: StrictUtilizationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictUtilizationRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictUtilizationRuleStatus): Boolean {
        when (status) {
            StrictUtilizationRuleStatus.REQUESTED -> return next == StrictUtilizationRuleStatus.GRANTED || next == StrictUtilizationRuleStatus.REVOKED
            StrictUtilizationRuleStatus.GRANTED -> return next == StrictUtilizationRuleStatus.EXPIRED
            StrictUtilizationRuleStatus.REVOKED -> {
                when (next) {
                    StrictUtilizationRuleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictUtilizationRuleStatus) {
        if (!canMoveTo(next)) {
            error("strict utilization rule $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round((floorValue + budget) * 100) / 100

    fun snapshot(): StrictUtilizationRule {
        return StrictUtilizationRule(
            code = code,
            title = title,
            budget = budget,
            priority = priority,
            floorValue = floorValue,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict utilization rule in status requested. */
fun newStrictUtilizationRule(code: String, title: String): StrictUtilizationRule {
    return StrictUtilizationRule(
        code = code,
        title = title,
        budget = 0.0,
        priority = 0,
        floorValue = 0.0,
        version = 0,
        status = StrictUtilizationRuleStatus.REQUESTED,
    )
}
