package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the parcel claim calculation. */
interface ParcelClaimFactor {
    fun describe(): String
    fun adjust(item: ParcelClaim, value: Double): Double
}

data class MinimumParcelClaimAdjuster(var minimum: Double) : ParcelClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingParcelClaimAdjuster(var step: Double) : ParcelClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelClaim, value: Double): Double = roundTo(value, step)
}

data class CapParcelClaimAdjuster(var cap: Double) : ParcelClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelClaim, value: Double): Double = minOf(value, cap)
}

data class PercentParcelClaimAdjuster(var percent: Double) : ParcelClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredParcelClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedParcelClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the parcel claim steps in order and keeps a trace of each. */
data class ParcelClaimEngine(
    var steps: MutableList<ParcelClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelClaim): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelClaimEngine(flags: Registry): ParcelClaimEngine {
    return ParcelClaimEngine(
        steps = mutableListOf<ParcelClaimFactor>(
            MinimumParcelClaimAdjuster(32.7),
            RoundingParcelClaimAdjuster(1.0),
            CapParcelClaimAdjuster(37879.0),
            PercentParcelClaimAdjuster(6.4),
            TieredParcelClaimAdjuster(
                mutableListOf<Double>(1667.0, 5138.0, 9718.0),
                mutableListOf<Double>(1.0, 1.09, 0.95),
            ),
            FlaggedParcelClaimAdjuster(flags, "parcel-claim-adaptive", 4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 94124.1,
    )
}
