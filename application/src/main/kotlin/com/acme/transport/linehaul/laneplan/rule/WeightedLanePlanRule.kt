package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.text.normalize

// The weighted lane plan rule records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a weighted lane plan rule is in its life cycle. */
enum class WeightedLanePlanRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a weighted lane plan rule and its current status. */
data class WeightedLanePlanRule(
    var code: String,
    var name: String,
    var visible: Boolean,
    var priority: Int,
    var shiftCode: String,
    var status: WeightedLanePlanRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLanePlanRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLanePlanRuleStatus): Boolean {
        when (status) {
            WeightedLanePlanRuleStatus.CREATED -> {
                when (next) {
                    WeightedLanePlanRuleStatus.PUBLISHED, WeightedLanePlanRuleStatus.RETIRED -> return true
                }
                return false
            }
            WeightedLanePlanRuleStatus.VALIDATED -> return next == WeightedLanePlanRuleStatus.RETIRED
            WeightedLanePlanRuleStatus.PUBLISHED -> return next == WeightedLanePlanRuleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedLanePlanRuleStatus) {
        if (!canMoveTo(next)) {
            error("weighted lane plan rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): WeightedLanePlanRule {
        return WeightedLanePlanRule(
            code = code,
            name = name,
            visible = visible,
            priority = priority,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted lane plan rule in status created. */
fun newWeightedLanePlanRule(code: String, name: String): WeightedLanePlanRule {
    return WeightedLanePlanRule(
        code = code,
        name = name,
        visible = false,
        priority = 0,
        shiftCode = "",
        status = WeightedLanePlanRuleStatus.CREATED,
    )
}
