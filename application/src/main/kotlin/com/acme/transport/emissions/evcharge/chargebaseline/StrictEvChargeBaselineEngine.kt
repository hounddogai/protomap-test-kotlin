package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict ev charge baseline calculation. */
interface StrictEvChargeBaselineFactor {
    fun describe(): String
    fun adjust(item: StrictEvChargeBaseline, value: Double): Double
}

data class TieredStrictEvChargeBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictEvChargeBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictEvChargeBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapStrictEvChargeBaselineAdjuster(var cap: Double) : StrictEvChargeBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictEvChargeBaseline, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictEvChargeBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictEvChargeBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictEvChargeBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictEvChargeBaselineAdjuster(var percent: Double) : StrictEvChargeBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictEvChargeBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleStrictEvChargeBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictEvChargeBaselineFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictEvChargeBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict ev charge baseline steps in order and keeps a trace of each. */
data class StrictEvChargeBaselineEngine(
    var steps: MutableList<StrictEvChargeBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictEvChargeBaseline): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictEvChargeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictEvChargeBaselineEngine(flags: Registry): StrictEvChargeBaselineEngine {
    return StrictEvChargeBaselineEngine(
        steps = mutableListOf<StrictEvChargeBaselineFactor>(
            TieredStrictEvChargeBaselineAdjuster(
                mutableListOf<Double>(5617.0, 8850.0, 9162.0, 9777.0),
                mutableListOf<Double>(0.97, 0.83, 0.96, 1.06),
            ),
            CapStrictEvChargeBaselineAdjuster(23959.0),
            FlaggedStrictEvChargeBaselineAdjuster(flags, "strict-ev-charge-baseline-rolling", -9.7),
            PercentStrictEvChargeBaselineAdjuster(16.9),
            TitleStrictEvChargeBaselineAdjuster(mutableMapOf<String, Double>("north" to 0.77)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 17895.2,
    )
}
