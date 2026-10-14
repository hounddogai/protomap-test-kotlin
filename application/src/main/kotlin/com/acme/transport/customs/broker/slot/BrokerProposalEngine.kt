package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the broker proposal calculation. */
interface BrokerProposalStep {
    fun describe(): String
    fun adjust(item: BrokerProposal, value: Double): Double
}

data class MinimumBrokerProposalAdjuster(var minimum: Double) : BrokerProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBrokerProposalAdjuster(var cap: Double) : BrokerProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerProposal, value: Double): Double = minOf(value, cap)
}

data class PercentBrokerProposalAdjuster(var percent: Double) : BrokerProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBrokerProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the broker proposal steps in order and keeps a trace of each. */
data class BrokerProposalEngine(
    var steps: MutableList<BrokerProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerProposal): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerProposalEngine(): BrokerProposalEngine {
    return BrokerProposalEngine(
        steps = mutableListOf<BrokerProposalStep>(
            MinimumBrokerProposalAdjuster(42.5),
            CapBrokerProposalAdjuster(38802.0),
            PercentBrokerProposalAdjuster(-11.5),
            TieredBrokerProposalAdjuster(
                mutableListOf<Double>(1893.0, 3519.0, 4281.0),
                mutableListOf<Double>(1.12, 0.97, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 51351.0,
    )
}
