package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional declaration target calculation. */
interface RegionalDeclarationTargetFactor {
    fun describe(): String
    fun adjust(item: RegionalDeclarationTarget, value: Double): Double
}

data class RoundingRegionalDeclarationTargetAdjuster(var step: Double) : RegionalDeclarationTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDeclarationTarget, value: Double): Double = roundTo(value, step)
}

data class NameRegionalDeclarationTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDeclarationTargetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalDeclarationTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalDeclarationTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDeclarationTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDeclarationTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalDeclarationTargetAdjuster(var percent: Double) : RegionalDeclarationTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDeclarationTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalDeclarationTargetAdjuster(var minimum: Double) : RegionalDeclarationTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDeclarationTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional declaration target steps in order and keeps a trace of each. */
data class RegionalDeclarationTargetEngine(
    var steps: MutableList<RegionalDeclarationTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDeclarationTarget): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDeclarationTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDeclarationTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDeclarationTargetEngine(): RegionalDeclarationTargetEngine {
    return RegionalDeclarationTargetEngine(
        steps = mutableListOf<RegionalDeclarationTargetFactor>(
            RoundingRegionalDeclarationTargetAdjuster(0.5),
            NameRegionalDeclarationTargetAdjuster(mutableMapOf<String, Double>("retail" to 0.82)),
            TieredRegionalDeclarationTargetAdjuster(
                mutableListOf<Double>(1485.0, 2586.0, 3470.0, 3780.0),
                mutableListOf<Double>(0.87, 1.0, 1.14, 0.83),
            ),
            PercentRegionalDeclarationTargetAdjuster(1.0),
            MinimumRegionalDeclarationTargetAdjuster(40.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 26245.8,
    )
}
