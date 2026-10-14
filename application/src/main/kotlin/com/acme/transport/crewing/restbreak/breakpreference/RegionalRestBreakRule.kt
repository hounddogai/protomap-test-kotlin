package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional rest break rule is in its life cycle. */
enum class RegionalRestBreakRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional rest break rule as the rest break team keeps it. */
data class RegionalRestBreakRule(
    var code: String,
    var label: String,
    var trailerId: String,
    var ownerId: String,
    var durationMinutes: Int,
    var priority: Int,
    var status: RegionalRestBreakRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRestBreakRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRestBreakRuleStatus): Boolean {
        when (status) {
            RegionalRestBreakRuleStatus.REQUESTED -> return next == RegionalRestBreakRuleStatus.EXPIRED
            RegionalRestBreakRuleStatus.GRANTED -> {
                when (next) {
                    RegionalRestBreakRuleStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalRestBreakRuleStatus.REVOKED -> return next == RegionalRestBreakRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalRestBreakRuleStatus) {
        if (!canMoveTo(next)) {
            error("regional rest break rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round(durationMinutes.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): RegionalRestBreakRule {
        return RegionalRestBreakRule(
            code = code,
            label = label,
            trailerId = trailerId,
            ownerId = ownerId,
            durationMinutes = durationMinutes,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional rest break rule in status requested. */
fun newRegionalRestBreakRule(code: String, label: String): RegionalRestBreakRule {
    return RegionalRestBreakRule(
        code = code,
        label = label,
        trailerId = "",
        ownerId = "",
        durationMinutes = 0,
        priority = 0,
        status = RegionalRestBreakRuleStatus.REQUESTED,
    )
}
