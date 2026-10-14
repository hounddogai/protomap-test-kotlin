package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient transit time budget calculation. */
interface LenientTransitTimeBudgetStep {
    fun describe(): String
    fun adjust(item: LenientTransitTimeBudget, value: Double): Double
}

data class CapLenientTransitTimeBudgetAdjuster(var cap: Double) : LenientTransitTimeBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientTransitTimeBudget, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientTransitTimeBudgetAdjuster(var step: Double) : LenientTransitTimeBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientTransitTimeBudget, value: Double): Double = roundTo(value, step)
}

data class TieredLenientTransitTimeBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientTransitTimeBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientTransitTimeBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientTransitTimeBudgetAdjuster(var minimum: Double) : LenientTransitTimeBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientTransitTimeBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientTransitTimeBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientTransitTimeBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientTransitTimeBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientTransitTimeBudgetThresholdOffsetAdjuster(var weight: Double) : LenientTransitTimeBudgetStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: LenientTransitTimeBudget, value: Double): Double = value + item.threshold * weight
}

/** Runs the lenient transit time budget steps in order and keeps a trace of each. */
data class LenientTransitTimeBudgetEngine(
    var steps: MutableList<LenientTransitTimeBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientTransitTimeBudget): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientTransitTimeBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientTransitTimeBudgetEngine(flags: Registry): LenientTransitTimeBudgetEngine {
    return LenientTransitTimeBudgetEngine(
        steps = mutableListOf<LenientTransitTimeBudgetStep>(
            CapLenientTransitTimeBudgetAdjuster(38103.0),
            RoundingLenientTransitTimeBudgetAdjuster(0.05),
            TieredLenientTransitTimeBudgetAdjuster(
                mutableListOf<Double>(6522.0, 8773.0),
                mutableListOf<Double>(1.15, 0.89),
            ),
            MinimumLenientTransitTimeBudgetAdjuster(32.5),
            FlaggedLenientTransitTimeBudgetAdjuster(flags, "lenient-transit-time-budget-global", -0.7),
            LenientTransitTimeBudgetThresholdOffsetAdjuster(0.26),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 17793.2,
    )
}
