package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional emission factor target calculation. */
interface RegionalEmissionFactorTargetAdjuster {
    fun describe(): String
    fun adjust(item: RegionalEmissionFactorTarget, value: Double): Double
}

data class MinimumRegionalEmissionFactorTargetAdjuster(
    var minimum: Double,
) : RegionalEmissionFactorTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalEmissionFactorTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalEmissionFactorTargetAdjuster(
    var percent: Double,
) : RegionalEmissionFactorTargetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalEmissionFactorTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameRegionalEmissionFactorTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalEmissionFactorTargetAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalEmissionFactorTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalEmissionFactorTargetAdjuster(
    var step: Double,
) : RegionalEmissionFactorTargetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalEmissionFactorTarget, value: Double): Double = roundTo(value, step)
}

/** Runs the regional emission factor target steps in order and keeps a trace of each. */
data class RegionalEmissionFactorTargetEngine(
    var steps: MutableList<RegionalEmissionFactorTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalEmissionFactorTarget): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalEmissionFactorTargetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalEmissionFactorTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalEmissionFactorTargetEngine(): RegionalEmissionFactorTargetEngine {
    return RegionalEmissionFactorTargetEngine(
        steps = mutableListOf<RegionalEmissionFactorTargetAdjuster>(
            MinimumRegionalEmissionFactorTargetAdjuster(34.8),
            PercentRegionalEmissionFactorTargetAdjuster(-1.3),
            NameRegionalEmissionFactorTargetAdjuster(mutableMapOf<String, Double>("north" to 1.29)),
            RoundingRegionalEmissionFactorTargetAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 24138.4,
    )
}
