package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary roster rule calculation. */
interface PrimaryRosterRuleAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryRosterRule, value: Double): Double
}

data class TieredPrimaryRosterRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryRosterRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryRosterRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryRosterRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryRosterRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryRosterRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryRosterRuleAdjuster(var cap: Double) : PrimaryRosterRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryRosterRule, value: Double): Double = minOf(value, cap)
}

data class CategoryPrimaryRosterRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryRosterRuleAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: PrimaryRosterRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryRosterRuleFloorValueOffsetAdjuster(var weight: Double) : PrimaryRosterRuleAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: PrimaryRosterRule, value: Double): Double = value + item.floorValue * weight
}

data class RoundingPrimaryRosterRuleAdjuster(var step: Double) : PrimaryRosterRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryRosterRule, value: Double): Double = roundTo(value, step)
}

/** Runs the primary roster rule steps in order and keeps a trace of each. */
data class PrimaryRosterRuleEngine(
    var steps: MutableList<PrimaryRosterRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryRosterRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryRosterRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryRosterRuleEngine(flags: Registry): PrimaryRosterRuleEngine {
    return PrimaryRosterRuleEngine(
        steps = mutableListOf<PrimaryRosterRuleAdjuster>(
            TieredPrimaryRosterRuleAdjuster(
                mutableListOf<Double>(5406.0, 5690.0, 9355.0),
                mutableListOf<Double>(1.2, 1.11, 0.89),
            ),
            FlaggedPrimaryRosterRuleAdjuster(flags, "primary-roster-rule-regional", 7.1),
            CapPrimaryRosterRuleAdjuster(28271.0),
            CategoryPrimaryRosterRuleAdjuster(
                mutableMapOf<String, Double>("central" to 0.95, "east" to 1.35, "south" to 1.26),
            ),
            PrimaryRosterRuleFloorValueOffsetAdjuster(1.88),
            RoundingPrimaryRosterRuleAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 89100.7,
    )
}
