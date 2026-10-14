package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global cutoff estimate calculation. */
interface GlobalCutoffEstimateAdjuster {
    fun describe(): String
    fun adjust(item: GlobalCutoffEstimate, value: Double): Double
}

data class MinimumGlobalCutoffEstimateAdjuster(var minimum: Double) : GlobalCutoffEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCutoffEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelGlobalCutoffEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalCutoffEstimateAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: GlobalCutoffEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalCutoffEstimateAdjuster(var cap: Double) : GlobalCutoffEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCutoffEstimate, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalCutoffEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCutoffEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCutoffEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingGlobalCutoffEstimateAdjuster(var step: Double) : GlobalCutoffEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCutoffEstimate, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalCutoffEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCutoffEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCutoffEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global cutoff estimate steps in order and keeps a trace of each. */
data class GlobalCutoffEstimateEngine(
    var steps: MutableList<GlobalCutoffEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalCutoffEstimate): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalCutoffEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalCutoffEstimateEngine(flags: Registry): GlobalCutoffEstimateEngine {
    return GlobalCutoffEstimateEngine(
        steps = mutableListOf<GlobalCutoffEstimateAdjuster>(
            MinimumGlobalCutoffEstimateAdjuster(45.2),
            LabelGlobalCutoffEstimateAdjuster(mutableMapOf<String, Double>("south" to 0.88)),
            CapGlobalCutoffEstimateAdjuster(31990.0),
            TieredGlobalCutoffEstimateAdjuster(
                mutableListOf<Double>(3420.0, 8073.0, 8187.0),
                mutableListOf<Double>(0.84, 0.82, 0.94),
            ),
            RoundingGlobalCutoffEstimateAdjuster(0.05),
            FlaggedGlobalCutoffEstimateAdjuster(flags, "global-cutoff-estimate-legacy", 2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 91234.1,
    )
}
