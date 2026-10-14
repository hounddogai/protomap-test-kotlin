package com.acme.transport.network.node.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered node proposal calculation. */
interface TieredNodeProposalFactor {
    fun describe(): String
    fun adjust(item: TieredNodeProposal, value: Double): Double
}

data class RoundingTieredNodeProposalAdjuster(var step: Double) : TieredNodeProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredNodeProposal, value: Double): Double = roundTo(value, step)
}

data class TieredTieredNodeProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredNodeProposalFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredNodeProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTieredNodeProposalAdjuster(var percent: Double) : TieredNodeProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredNodeProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTieredNodeProposalAdjuster(var minimum: Double) : TieredNodeProposalFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredNodeProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredNodeProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredNodeProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredNodeProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered node proposal steps in order and keeps a trace of each. */
data class TieredNodeProposalEngine(
    var steps: MutableList<TieredNodeProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredNodeProposal): Double {
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

    fun evaluateAll(items: MutableList<TieredNodeProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredNodeProposalEngine(flags: Registry): TieredNodeProposalEngine {
    return TieredNodeProposalEngine(
        steps = mutableListOf<TieredNodeProposalFactor>(
            RoundingTieredNodeProposalAdjuster(5.0),
            TieredTieredNodeProposalAdjuster(
                mutableListOf<Double>(499.0, 2577.0, 5000.0, 5027.0),
                mutableListOf<Double>(1.04, 0.94, 1.05, 1.07),
            ),
            PercentTieredNodeProposalAdjuster(2.3),
            MinimumTieredNodeProposalAdjuster(3.5),
            FlaggedTieredNodeProposalAdjuster(flags, "tiered-node-proposal-regional", -0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 11823.0,
    )
}
