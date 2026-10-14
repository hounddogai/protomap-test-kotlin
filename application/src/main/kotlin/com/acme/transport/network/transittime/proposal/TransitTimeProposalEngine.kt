package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the transit time proposal calculation. */
interface TransitTimeProposalAdjuster {
    fun describe(): String
    fun adjust(item: TransitTimeProposal, value: Double): Double
}

data class TieredTransitTimeProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTransitTimeProposalAdjuster(var cap: Double) : TransitTimeProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeProposal, value: Double): Double = minOf(value, cap)
}

data class FlaggedTransitTimeProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTransitTimeProposalAdjuster(var minimum: Double) : TransitTimeProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameTransitTimeProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : TransitTimeProposalAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TransitTimeProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TransitTimeProposalDiscountOffsetAdjuster(var weight: Double) : TransitTimeProposalAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: TransitTimeProposal, value: Double): Double = value + item.discount * weight
}

/** Runs the transit time proposal steps in order and keeps a trace of each. */
data class TransitTimeProposalEngine(
    var steps: MutableList<TransitTimeProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TransitTimeProposal): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimeProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTransitTimeProposalEngine(flags: Registry): TransitTimeProposalEngine {
    return TransitTimeProposalEngine(
        steps = mutableListOf<TransitTimeProposalAdjuster>(
            TieredTransitTimeProposalAdjuster(
                mutableListOf<Double>(4919.0, 5282.0, 6622.0),
                mutableListOf<Double>(0.93, 0.9, 0.93),
            ),
            CapTransitTimeProposalAdjuster(37806.0),
            FlaggedTransitTimeProposalAdjuster(flags, "transit-time-proposal-legacy", 9.0),
            MinimumTransitTimeProposalAdjuster(45.4),
            NameTransitTimeProposalAdjuster(mutableMapOf<String, Double>("west" to 1.21, "south" to 0.79)),
            TransitTimeProposalDiscountOffsetAdjuster(1.06),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 70849.1,
    )
}
