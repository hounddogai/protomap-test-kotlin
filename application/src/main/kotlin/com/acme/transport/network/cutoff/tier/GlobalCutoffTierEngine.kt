package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global cutoff tier calculation. */
interface GlobalCutoffTierAdjuster {
    fun describe(): String
    fun adjust(item: GlobalCutoffTier, value: Double): Double
}

data class MinimumGlobalCutoffTierAdjuster(var minimum: Double) : GlobalCutoffTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCutoffTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalCutoffTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCutoffTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCutoffTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentGlobalCutoffTierAdjuster(var percent: Double) : GlobalCutoffTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCutoffTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalCutoffTierBudgetOffsetAdjuster(var weight: Double) : GlobalCutoffTierAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: GlobalCutoffTier, value: Double): Double = value + item.budget * weight
}

/** Runs the global cutoff tier steps in order and keeps a trace of each. */
data class GlobalCutoffTierEngine(
    var steps: MutableList<GlobalCutoffTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalCutoffTier): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalCutoffTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalCutoffTierEngine(flags: Registry): GlobalCutoffTierEngine {
    return GlobalCutoffTierEngine(
        steps = mutableListOf<GlobalCutoffTierAdjuster>(
            MinimumGlobalCutoffTierAdjuster(41.3),
            FlaggedGlobalCutoffTierAdjuster(flags, "global-cutoff-tier-scheduled", -3.9),
            PercentGlobalCutoffTierAdjuster(21.3),
            GlobalCutoffTierBudgetOffsetAdjuster(0.67),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 49412.4,
    )
}
