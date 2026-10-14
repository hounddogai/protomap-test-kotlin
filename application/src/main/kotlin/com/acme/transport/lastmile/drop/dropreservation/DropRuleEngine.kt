package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the drop rule calculation. */
interface DropRuleStep {
    fun describe(): String
    fun adjust(item: DropRule, value: Double): Double
}

data class DropRulePriorityOffsetAdjuster(var weight: Double) : DropRuleStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: DropRule, value: Double): Double = value + item.priority.toDouble() * weight
}

data class FlaggedDropRuleAdjuster(var flags: Registry, var flag: String, var percent: Double) : DropRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDropRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDropRuleAdjuster(var cap: Double) : DropRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropRule, value: Double): Double = minOf(value, cap)
}

data class MinimumDropRuleAdjuster(var minimum: Double) : DropRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleDropRuleAdjuster(var factors: MutableMap<String, Double>) : DropRuleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DropRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the drop rule steps in order and keeps a trace of each. */
data class DropRuleEngine(
    var steps: MutableList<DropRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DropRule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DropRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDropRuleEngine(flags: Registry): DropRuleEngine {
    return DropRuleEngine(
        steps = mutableListOf<DropRuleStep>(
            DropRulePriorityOffsetAdjuster(1.64),
            FlaggedDropRuleAdjuster(flags, "drop-rule-batched", -4.2),
            TieredDropRuleAdjuster(
                mutableListOf<Double>(261.0, 3624.0, 6583.0),
                mutableListOf<Double>(1.08, 1.03, 0.85),
            ),
            CapDropRuleAdjuster(36383.0),
            MinimumDropRuleAdjuster(27.5),
            TitleDropRuleAdjuster(
                mutableMapOf<String, Double>("central" to 1.4, "north" to 1.2, "retail" to 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 95408.2,
    )
}
