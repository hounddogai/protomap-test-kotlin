package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback hub mapping calculation. */
interface FallbackHubMappingFactor {
    fun describe(): String
    fun adjust(item: FallbackHubMapping, value: Double): Double
}

data class MinimumFallbackHubMappingAdjuster(var minimum: Double) : FallbackHubMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackHubMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackHubMappingEtaMinutesOffsetAdjuster(var weight: Double) : FallbackHubMappingFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: FallbackHubMapping, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class FlaggedFallbackHubMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackHubMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackHubMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackHubMappingAdjuster(var percent: Double) : FallbackHubMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackHubMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackHubMappingAdjuster(var cap: Double) : FallbackHubMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackHubMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback hub mapping steps in order and keeps a trace of each. */
data class FallbackHubMappingEngine(
    var steps: MutableList<FallbackHubMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackHubMapping): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackHubMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackHubMappingEngine(flags: Registry): FallbackHubMappingEngine {
    return FallbackHubMappingEngine(
        steps = mutableListOf<FallbackHubMappingFactor>(
            MinimumFallbackHubMappingAdjuster(6.1),
            FallbackHubMappingEtaMinutesOffsetAdjuster(1.76),
            FlaggedFallbackHubMappingAdjuster(flags, "fallback-hub-mapping-adaptive", 1.3),
            PercentFallbackHubMappingAdjuster(-2.9),
            CapFallbackHubMappingAdjuster(47914.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 31050.5,
    )
}
