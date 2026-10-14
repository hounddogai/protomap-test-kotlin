package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional sortation adjustment calculation. */
interface RegionalSortationAdjustmentModifier {
    fun describe(): String
    fun adjust(item: RegionalSortationAdjustment, value: Double): Double
}

data class RegionalSortationAdjustmentStopCountOffsetAdjuster(
    var weight: Double,
) : RegionalSortationAdjustmentModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RegionalSortationAdjustment, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class CapRegionalSortationAdjustmentAdjuster(var cap: Double) : RegionalSortationAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalSortationAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalSortationAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalSortationAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalSortationAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalSortationAdjustmentAdjuster(
    var percent: Double,
) : RegionalSortationAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalSortationAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalSortationAdjustmentAdjuster(var step: Double) : RegionalSortationAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalSortationAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the regional sortation adjustment steps in order and keeps a trace of each. */
data class RegionalSortationAdjustmentEngine(
    var steps: MutableList<RegionalSortationAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalSortationAdjustment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalSortationAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalSortationAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalSortationAdjustmentEngine(): RegionalSortationAdjustmentEngine {
    return RegionalSortationAdjustmentEngine(
        steps = mutableListOf<RegionalSortationAdjustmentModifier>(
            RegionalSortationAdjustmentStopCountOffsetAdjuster(1.0),
            CapRegionalSortationAdjustmentAdjuster(10821.0),
            TieredRegionalSortationAdjustmentAdjuster(
                mutableListOf<Double>(4285.0, 8881.0),
                mutableListOf<Double>(0.84, 1.11),
            ),
            PercentRegionalSortationAdjustmentAdjuster(2.7),
            RoundingRegionalSortationAdjustmentAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 42440.7,
    )
}
