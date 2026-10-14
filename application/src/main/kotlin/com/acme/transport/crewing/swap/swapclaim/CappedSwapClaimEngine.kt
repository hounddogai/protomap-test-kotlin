package com.acme.transport.crewing.swap.swapclaim

import com.acme.transport.kit.flags.Registry

/** One step of the capped swap claim calculation. */
interface CappedSwapClaimModifier {
    fun describe(): String
    fun adjust(item: CappedSwapClaim, value: Double): Double
}

data class FlaggedCappedSwapClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedSwapClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedSwapClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedSwapClaimAdjuster(var percent: Double) : CappedSwapClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedSwapClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCappedSwapClaimAdjuster(var minimum: Double) : CappedSwapClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedSwapClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedSwapClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedSwapClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedSwapClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CappedSwapClaimRevisionOffsetAdjuster(var weight: Double) : CappedSwapClaimModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: CappedSwapClaim, value: Double): Double = value + item.revision.toDouble() * weight
}

data class CapCappedSwapClaimAdjuster(var cap: Double) : CappedSwapClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedSwapClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the capped swap claim steps in order and keeps a trace of each. */
data class CappedSwapClaimEngine(
    var steps: MutableList<CappedSwapClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedSwapClaim): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedSwapClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedSwapClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedSwapClaimEngine(flags: Registry): CappedSwapClaimEngine {
    return CappedSwapClaimEngine(
        steps = mutableListOf<CappedSwapClaimModifier>(
            FlaggedCappedSwapClaimAdjuster(flags, "capped-swap-claim-regional", -0.3),
            PercentCappedSwapClaimAdjuster(8.8),
            MinimumCappedSwapClaimAdjuster(10.5),
            TieredCappedSwapClaimAdjuster(
                mutableListOf<Double>(289.0, 1378.0, 5408.0),
                mutableListOf<Double>(1.19, 1.16, 0.97),
            ),
            CappedSwapClaimRevisionOffsetAdjuster(0.09),
            CapCappedSwapClaimAdjuster(23056.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 48270.9,
    )
}
