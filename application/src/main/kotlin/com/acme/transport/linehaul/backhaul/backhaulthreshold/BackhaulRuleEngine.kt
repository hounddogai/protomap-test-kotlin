package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the backhaul rule calculation. */
interface BackhaulRuleStep {
    fun describe(): String
    fun adjust(item: BackhaulRule, value: Double): Double
}

data class RoundingBackhaulRuleAdjuster(var step: Double) : BackhaulRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulRule, value: Double): Double = roundTo(value, step)
}

data class BackhaulRuleWindowDaysOffsetAdjuster(var weight: Double) : BackhaulRuleStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: BackhaulRule, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class PercentBackhaulRuleAdjuster(var percent: Double) : BackhaulRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBackhaulRuleAdjuster(var minimum: Double) : BackhaulRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameBackhaulRuleAdjuster(var factors: MutableMap<String, Double>) : BackhaulRuleStep {
    override fun describe(): String = "by name"

    override fun adjust(item: BackhaulRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBackhaulRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the backhaul rule steps in order and keeps a trace of each. */
data class BackhaulRuleEngine(
    var steps: MutableList<BackhaulRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BackhaulRule): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulRuleStep = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBackhaulRuleEngine(): BackhaulRuleEngine {
    return BackhaulRuleEngine(
        steps = mutableListOf<BackhaulRuleStep>(
            RoundingBackhaulRuleAdjuster(0.5),
            BackhaulRuleWindowDaysOffsetAdjuster(1.65),
            PercentBackhaulRuleAdjuster(-23.2),
            MinimumBackhaulRuleAdjuster(28.5),
            NameBackhaulRuleAdjuster(
                mutableMapOf<String, Double>("north" to 1.08, "central" to 0.78, "south" to 0.81),
            ),
            TieredBackhaulRuleAdjuster(
                mutableListOf<Double>(316.0, 804.0, 6342.0, 7758.0),
                mutableListOf<Double>(1.2, 0.99, 0.99, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 39675.3,
    )
}
