package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped cutoff threshold calculation. */
interface CappedCutoffThresholdModifier {
    fun describe(): String
    fun adjust(item: CappedCutoffThreshold, value: Double): Double
}

data class PercentCappedCutoffThresholdAdjuster(var percent: Double) : CappedCutoffThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCutoffThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedCutoffThresholdAdjuster(var step: Double) : CappedCutoffThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCutoffThreshold, value: Double): Double = roundTo(value, step)
}

data class CappedCutoffThresholdPalletsOffsetAdjuster(var weight: Double) : CappedCutoffThresholdModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CappedCutoffThreshold, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class FlaggedCappedCutoffThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCutoffThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCutoffThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedCutoffThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCutoffThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCutoffThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelCappedCutoffThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCutoffThresholdModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedCutoffThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped cutoff threshold steps in order and keeps a trace of each. */
data class CappedCutoffThresholdEngine(
    var steps: MutableList<CappedCutoffThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedCutoffThreshold): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCutoffThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCutoffThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedCutoffThresholdEngine(flags: Registry): CappedCutoffThresholdEngine {
    return CappedCutoffThresholdEngine(
        steps = mutableListOf<CappedCutoffThresholdModifier>(
            PercentCappedCutoffThresholdAdjuster(-5.9),
            RoundingCappedCutoffThresholdAdjuster(0.1),
            CappedCutoffThresholdPalletsOffsetAdjuster(0.88),
            FlaggedCappedCutoffThresholdAdjuster(flags, "capped-cutoff-threshold-seasonal", 8.8),
            TieredCappedCutoffThresholdAdjuster(
                mutableListOf<Double>(1159.0, 2153.0, 6100.0),
                mutableListOf<Double>(0.88, 1.04, 0.99),
            ),
            LabelCappedCutoffThresholdAdjuster(mutableMapOf<String, Double>("west" to 1.36, "south" to 1.37)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 23285.1,
    )
}
