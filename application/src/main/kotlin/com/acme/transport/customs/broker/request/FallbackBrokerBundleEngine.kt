package com.acme.transport.customs.broker.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback broker bundle calculation. */
interface FallbackBrokerBundleStep {
    fun describe(): String
    fun adjust(item: FallbackBrokerBundle, value: Double): Double
}

data class FallbackBrokerBundleSequenceOffsetAdjuster(var weight: Double) : FallbackBrokerBundleStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: FallbackBrokerBundle, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class CapFallbackBrokerBundleAdjuster(var cap: Double) : FallbackBrokerBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackBrokerBundle, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackBrokerBundleAdjuster(var percent: Double) : FallbackBrokerBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackBrokerBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryFallbackBrokerBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackBrokerBundleStep {
    override fun describe(): String = "by country"

    override fun adjust(item: FallbackBrokerBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackBrokerBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackBrokerBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackBrokerBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFallbackBrokerBundleAdjuster(var step: Double) : FallbackBrokerBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackBrokerBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback broker bundle steps in order and keeps a trace of each. */
data class FallbackBrokerBundleEngine(
    var steps: MutableList<FallbackBrokerBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackBrokerBundle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackBrokerBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackBrokerBundleEngine(flags: Registry): FallbackBrokerBundleEngine {
    return FallbackBrokerBundleEngine(
        steps = mutableListOf<FallbackBrokerBundleStep>(
            FallbackBrokerBundleSequenceOffsetAdjuster(0.16),
            CapFallbackBrokerBundleAdjuster(2722.0),
            PercentFallbackBrokerBundleAdjuster(16.6),
            CountryFallbackBrokerBundleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.22, "south" to 1.11, "central" to 1.22),
            ),
            FlaggedFallbackBrokerBundleAdjuster(flags, "fallback-broker-bundle-manual", -8.8),
            RoundingFallbackBrokerBundleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 89357.7,
    )
}
