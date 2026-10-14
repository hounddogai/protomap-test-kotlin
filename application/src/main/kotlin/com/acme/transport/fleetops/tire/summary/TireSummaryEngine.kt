package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tire summary calculation. */
interface TireSummaryModifier {
    fun describe(): String
    fun adjust(item: TireSummary, value: Double): Double
}

data class RoundingTireSummaryAdjuster(var step: Double) : TireSummaryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireSummary, value: Double): Double = roundTo(value, step)
}

data class MinimumTireSummaryAdjuster(var minimum: Double) : TireSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTireSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTireSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireSummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tire summary steps in order and keeps a trace of each. */
data class TireSummaryEngine(
    var steps: MutableList<TireSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireSummary): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TireSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireSummaryEngine(flags: Registry): TireSummaryEngine {
    return TireSummaryEngine(
        steps = mutableListOf<TireSummaryModifier>(
            RoundingTireSummaryAdjuster(0.25),
            MinimumTireSummaryAdjuster(41.6),
            TieredTireSummaryAdjuster(mutableListOf<Double>(1492.0, 8476.0), mutableListOf<Double>(1.03, 0.87)),
            FlaggedTireSummaryAdjuster(flags, "tire-summary-batched", 4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 70566.1,
    )
}
