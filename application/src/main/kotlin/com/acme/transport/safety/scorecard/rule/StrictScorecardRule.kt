package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict scorecard rule records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a strict scorecard rule is in its life cycle. */
enum class StrictScorecardRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict scorecard rule. Its code is unique within a tenant. */
data class StrictScorecardRule(
    var code: String,
    var name: String,
    var sequence: Int,
    var floorValue: Double,
    var title: String,
    var region: String,
    var depot: String,
    var maximum: Double,
    var windowDays: Int,
    var category: String,
    var status: StrictScorecardRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictScorecardRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictScorecardRuleStatus): Boolean {
        when (status) {
            StrictScorecardRuleStatus.DRAFT -> {
                when (next) {
                    StrictScorecardRuleStatus.PENDING, StrictScorecardRuleStatus.CLOSED -> return true
                }
                return false
            }
            StrictScorecardRuleStatus.PENDING -> return next == StrictScorecardRuleStatus.ACTIVE
            StrictScorecardRuleStatus.ACTIVE -> return next == StrictScorecardRuleStatus.SUSPENDED
            StrictScorecardRuleStatus.SUSPENDED -> {
                when (next) {
                    StrictScorecardRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictScorecardRuleStatus) {
        if (!canMoveTo(next)) {
            error("strict scorecard rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((floorValue + maximum) * 100) / 100

    fun snapshot(): StrictScorecardRule {
        return StrictScorecardRule(
            code = code,
            name = name,
            sequence = sequence,
            floorValue = floorValue,
            title = title,
            region = region,
            depot = depot,
            maximum = maximum,
            windowDays = windowDays,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict scorecard rule in status draft. */
fun newStrictScorecardRule(code: String, name: String): StrictScorecardRule {
    return StrictScorecardRule(
        code = code,
        name = name,
        sequence = 0,
        floorValue = 0.0,
        title = "",
        region = "",
        depot = "",
        maximum = 0.0,
        windowDays = 0,
        category = "",
        status = StrictScorecardRuleStatus.DRAFT,
    )
}
