package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global fuel burn digest calculation. */
interface GlobalFuelBurnDigestFactor {
    fun describe(): String
    fun adjust(item: GlobalFuelBurnDigest, value: Double): Double
}

data class RoundingGlobalFuelBurnDigestAdjuster(var step: Double) : GlobalFuelBurnDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalFuelBurnDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalFuelBurnDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalFuelBurnDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalFuelBurnDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalFuelBurnDigestCostOffsetAdjuster(var weight: Double) : GlobalFuelBurnDigestFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: GlobalFuelBurnDigest, value: Double): Double = value + item.cost * weight
}

data class MinimumGlobalFuelBurnDigestAdjuster(var minimum: Double) : GlobalFuelBurnDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalFuelBurnDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global fuel burn digest steps in order and keeps a trace of each. */
data class GlobalFuelBurnDigestEngine(
    var steps: MutableList<GlobalFuelBurnDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalFuelBurnDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalFuelBurnDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalFuelBurnDigestEngine(flags: Registry): GlobalFuelBurnDigestEngine {
    return GlobalFuelBurnDigestEngine(
        steps = mutableListOf<GlobalFuelBurnDigestFactor>(
            RoundingGlobalFuelBurnDigestAdjuster(0.25),
            FlaggedGlobalFuelBurnDigestAdjuster(flags, "global-fuel-burn-digest-weighted", -3.8),
            GlobalFuelBurnDigestCostOffsetAdjuster(1.42),
            MinimumGlobalFuelBurnDigestAdjuster(49.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 73962.3,
    )
}
