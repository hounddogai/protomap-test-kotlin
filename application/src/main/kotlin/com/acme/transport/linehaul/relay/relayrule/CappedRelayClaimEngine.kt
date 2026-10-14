package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped relay claim calculation. */
interface CappedRelayClaimFactor {
    fun describe(): String
    fun adjust(item: CappedRelayClaim, value: Double): Double
}

data class FlaggedCappedRelayClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedRelayClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedRelayClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedRelayClaimAdjuster(var step: Double) : CappedRelayClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRelayClaim, value: Double): Double = roundTo(value, step)
}

data class CapCappedRelayClaimAdjuster(var cap: Double) : CappedRelayClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedRelayClaim, value: Double): Double = minOf(value, cap)
}

data class TieredCappedRelayClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRelayClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRelayClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedRelayClaimAdjuster(var minimum: Double) : CappedRelayClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRelayClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneCappedRelayClaimAdjuster(var factors: MutableMap<String, Double>) : CappedRelayClaimFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: CappedRelayClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped relay claim steps in order and keeps a trace of each. */
data class CappedRelayClaimEngine(
    var steps: MutableList<CappedRelayClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedRelayClaim): Double {
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

    fun evaluateAll(items: MutableList<CappedRelayClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedRelayClaimEngine(flags: Registry): CappedRelayClaimEngine {
    return CappedRelayClaimEngine(
        steps = mutableListOf<CappedRelayClaimFactor>(
            FlaggedCappedRelayClaimAdjuster(flags, "capped-relay-claim-capped", 3.1),
            RoundingCappedRelayClaimAdjuster(0.1),
            CapCappedRelayClaimAdjuster(46599.0),
            TieredCappedRelayClaimAdjuster(
                mutableListOf<Double>(2746.0, 4791.0),
                mutableListOf<Double>(1.12, 0.86),
            ),
            MinimumCappedRelayClaimAdjuster(6.7),
            LaneCappedRelayClaimAdjuster(mutableMapOf<String, Double>("north" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 75236.2,
    )
}
