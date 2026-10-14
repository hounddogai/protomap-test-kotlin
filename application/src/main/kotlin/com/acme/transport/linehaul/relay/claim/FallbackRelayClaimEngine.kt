package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback relay claim calculation. */
interface FallbackRelayClaimAdjuster {
    fun describe(): String
    fun adjust(item: FallbackRelayClaim, value: Double): Double
}

data class PercentFallbackRelayClaimAdjuster(var percent: Double) : FallbackRelayClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRelayClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackRelayClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRelayClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRelayClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackRelayClaimAdjuster(var minimum: Double) : FallbackRelayClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRelayClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackRelayClaimAdjuster(var step: Double) : FallbackRelayClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRelayClaim, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackRelayClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRelayClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRelayClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFallbackRelayClaimAdjuster(var cap: Double) : FallbackRelayClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRelayClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback relay claim steps in order and keeps a trace of each. */
data class FallbackRelayClaimEngine(
    var steps: MutableList<FallbackRelayClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRelayClaim): Double {
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

    fun evaluateAll(items: MutableList<FallbackRelayClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRelayClaimEngine(flags: Registry): FallbackRelayClaimEngine {
    return FallbackRelayClaimEngine(
        steps = mutableListOf<FallbackRelayClaimAdjuster>(
            PercentFallbackRelayClaimAdjuster(-12.4),
            FlaggedFallbackRelayClaimAdjuster(flags, "fallback-relay-claim-default", 6.5),
            MinimumFallbackRelayClaimAdjuster(2.4),
            RoundingFallbackRelayClaimAdjuster(0.05),
            TieredFallbackRelayClaimAdjuster(
                mutableListOf<Double>(5033.0, 5281.0, 5354.0, 9717.0),
                mutableListOf<Double>(0.92, 1.15, 0.89, 0.98),
            ),
            CapFallbackRelayClaimAdjuster(40161.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 93855.3,
    )
}
