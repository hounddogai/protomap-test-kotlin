package com.acme.transport.crewing.roster.rule

/** One step of the roster rule calculation. */
interface RosterRuleAdjuster {
    fun describe(): String
    fun adjust(item: RosterRule, value: Double): Double
}

data class PercentRosterRuleAdjuster(var percent: Double) : RosterRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRosterRuleAdjuster(var minimum: Double) : RosterRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRosterRuleAdjuster(var cap: Double) : RosterRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterRule, value: Double): Double = minOf(value, cap)
}

data class TitleRosterRuleAdjuster(var factors: MutableMap<String, Double>) : RosterRuleAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RosterRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the roster rule steps in order and keeps a trace of each. */
data class RosterRuleEngine(
    var steps: MutableList<RosterRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RosterRule): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterRuleAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRosterRuleEngine(): RosterRuleEngine {
    return RosterRuleEngine(
        steps = mutableListOf<RosterRuleAdjuster>(
            PercentRosterRuleAdjuster(5.0),
            MinimumRosterRuleAdjuster(30.0),
            CapRosterRuleAdjuster(14494.0),
            TitleRosterRuleAdjuster(mutableMapOf<String, Double>("north" to 1.09)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 90093.3,
    )
}
