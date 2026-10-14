package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default hub tier calculation. */
interface DefaultHubTierStep {
    fun describe(): String
    fun adjust(item: DefaultHubTier, value: Double): Double
}

data class TieredDefaultHubTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultHubTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultHubTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultHubTierMinimumOffsetAdjuster(var weight: Double) : DefaultHubTierStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: DefaultHubTier, value: Double): Double = value + item.minimum * weight
}

data class FlaggedDefaultHubTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultHubTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultHubTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default hub tier steps in order and keeps a trace of each. */
data class DefaultHubTierEngine(
    var steps: MutableList<DefaultHubTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultHubTier): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultHubTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultHubTierEngine(flags: Registry): DefaultHubTierEngine {
    return DefaultHubTierEngine(
        steps = mutableListOf<DefaultHubTierStep>(
            TieredDefaultHubTierAdjuster(
                mutableListOf<Double>(1053.0, 3604.0, 6168.0, 7816.0),
                mutableListOf<Double>(1.06, 1.11, 1.08, 1.01),
            ),
            DefaultHubTierMinimumOffsetAdjuster(1.22),
            FlaggedDefaultHubTierAdjuster(flags, "default-hub-tier-legacy", -6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 98090.7,
    )
}
