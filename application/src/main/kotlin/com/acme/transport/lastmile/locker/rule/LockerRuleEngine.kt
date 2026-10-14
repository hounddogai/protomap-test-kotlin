package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.flags.Registry

/** One step of the locker rule calculation. */
interface LockerRuleStep {
    fun describe(): String
    fun adjust(item: LockerRule, value: Double): Double
}

data class LockerRuleVersionOffsetAdjuster(var weight: Double) : LockerRuleStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: LockerRule, value: Double): Double = value + item.version.toDouble() * weight
}

data class CapLockerRuleAdjuster(var cap: Double) : LockerRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerRule, value: Double): Double = minOf(value, cap)
}

data class UnitLockerRuleAdjuster(var factors: MutableMap<String, Double>) : LockerRuleStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: LockerRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLockerRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLockerRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the locker rule steps in order and keeps a trace of each. */
data class LockerRuleEngine(
    var steps: MutableList<LockerRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LockerRule): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerRuleStep = steps[index]
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

    fun evaluateAll(items: MutableList<LockerRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLockerRuleEngine(flags: Registry): LockerRuleEngine {
    return LockerRuleEngine(
        steps = mutableListOf<LockerRuleStep>(
            LockerRuleVersionOffsetAdjuster(1.9),
            CapLockerRuleAdjuster(13225.0),
            UnitLockerRuleAdjuster(
                mutableMapOf<String, Double>("online" to 0.97, "east" to 0.87, "central" to 0.79),
            ),
            FlaggedLockerRuleAdjuster(flags, "locker-rule-primary", 0.9),
            TieredLockerRuleAdjuster(mutableListOf<Double>(4704.0, 9776.0), mutableListOf<Double>(1.17, 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 90324.8,
    )
}
