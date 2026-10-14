package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge proposal calculation. */
interface FuelSurchargeProposalFactor {
    fun describe(): String
    fun adjust(item: FuelSurchargeProposal, value: Double): Double
}

data class RoundingFuelSurchargeProposalAdjuster(var step: Double) : FuelSurchargeProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeProposal, value: Double): Double = roundTo(value, step)
}

data class MinimumFuelSurchargeProposalAdjuster(var minimum: Double) : FuelSurchargeProposalFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFuelSurchargeProposalAdjuster(var cap: Double) : FuelSurchargeProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel surcharge proposal steps in order and keeps a trace of each. */
data class FuelSurchargeProposalEngine(
    var steps: MutableList<FuelSurchargeProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelSurchargeProposal): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeProposalFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelSurchargeProposalEngine(): FuelSurchargeProposalEngine {
    return FuelSurchargeProposalEngine(
        steps = mutableListOf<FuelSurchargeProposalFactor>(
            RoundingFuelSurchargeProposalAdjuster(0.5),
            MinimumFuelSurchargeProposalAdjuster(22.1),
            CapFuelSurchargeProposalAdjuster(39589.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 33253.2,
    )
}
