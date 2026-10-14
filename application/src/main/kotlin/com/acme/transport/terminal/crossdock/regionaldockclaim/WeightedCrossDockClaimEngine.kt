package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.flags.Registry

/** One step of the weighted cross dock claim calculation. */
interface WeightedCrossDockClaimFactor {
    fun describe(): String
    fun adjust(item: WeightedCrossDockClaim, value: Double): Double
}

data class TieredWeightedCrossDockClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedCrossDockClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedCrossDockClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedCrossDockClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCrossDockClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCrossDockClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelWeightedCrossDockClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCrossDockClaimFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: WeightedCrossDockClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedCrossDockClaimAdjuster(var minimum: Double) : WeightedCrossDockClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCrossDockClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedCrossDockClaimAdjuster(var cap: Double) : WeightedCrossDockClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCrossDockClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted cross dock claim steps in order and keeps a trace of each. */
data class WeightedCrossDockClaimEngine(
    var steps: MutableList<WeightedCrossDockClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedCrossDockClaim): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCrossDockClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCrossDockClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedCrossDockClaimEngine(flags: Registry): WeightedCrossDockClaimEngine {
    return WeightedCrossDockClaimEngine(
        steps = mutableListOf<WeightedCrossDockClaimFactor>(
            TieredWeightedCrossDockClaimAdjuster(
                mutableListOf<Double>(994.0, 3047.0, 5271.0, 7216.0),
                mutableListOf<Double>(0.9, 1.12, 0.95, 1.13),
            ),
            FlaggedWeightedCrossDockClaimAdjuster(flags, "weighted-cross-dock-claim-default", -0.7),
            ChannelWeightedCrossDockClaimAdjuster(
                mutableMapOf<String, Double>("online" to 0.84, "west" to 0.82, "south" to 1.35),
            ),
            MinimumWeightedCrossDockClaimAdjuster(14.2),
            CapWeightedCrossDockClaimAdjuster(30025.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 36519.4,
    )
}
