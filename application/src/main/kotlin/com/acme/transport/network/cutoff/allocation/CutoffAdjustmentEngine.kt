package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cutoff adjustment calculation. */
interface CutoffAdjustmentModifier {
    fun describe(): String
    fun adjust(item: CutoffAdjustment, value: Double): Double
}

data class RoundingCutoffAdjustmentAdjuster(var step: Double) : CutoffAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredCutoffAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCutoffAdjustmentAdjuster(var minimum: Double) : CutoffAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCutoffAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the cutoff adjustment steps in order and keeps a trace of each. */
data class CutoffAdjustmentEngine(
    var steps: MutableList<CutoffAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffAdjustment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffAdjustmentEngine(flags: Registry): CutoffAdjustmentEngine {
    return CutoffAdjustmentEngine(
        steps = mutableListOf<CutoffAdjustmentModifier>(
            RoundingCutoffAdjustmentAdjuster(0.5),
            TieredCutoffAdjustmentAdjuster(
                mutableListOf<Double>(926.0, 1910.0, 3139.0, 9571.0),
                mutableListOf<Double>(0.82, 1.16, 1.04, 1.01),
            ),
            MinimumCutoffAdjustmentAdjuster(25.6),
            FlaggedCutoffAdjustmentAdjuster(flags, "cutoff-adjustment-capped", -0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 97112.4,
    )
}
