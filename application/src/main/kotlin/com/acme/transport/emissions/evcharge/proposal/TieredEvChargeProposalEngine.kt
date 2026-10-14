package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered ev charge proposal calculation. */
interface TieredEvChargeProposalModifier {
    fun describe(): String
    fun adjust(item: TieredEvChargeProposal, value: Double): Double
}

data class PercentTieredEvChargeProposalAdjuster(var percent: Double) : TieredEvChargeProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredEvChargeProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitTieredEvChargeProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredEvChargeProposalModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: TieredEvChargeProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredEvChargeProposalQuantityOffsetAdjuster(var weight: Double) : TieredEvChargeProposalModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: TieredEvChargeProposal, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class CapTieredEvChargeProposalAdjuster(var cap: Double) : TieredEvChargeProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredEvChargeProposal, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredEvChargeProposalAdjuster(var step: Double) : TieredEvChargeProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredEvChargeProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered ev charge proposal steps in order and keeps a trace of each. */
data class TieredEvChargeProposalEngine(
    var steps: MutableList<TieredEvChargeProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredEvChargeProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredEvChargeProposalModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredEvChargeProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredEvChargeProposalEngine(): TieredEvChargeProposalEngine {
    return TieredEvChargeProposalEngine(
        steps = mutableListOf<TieredEvChargeProposalModifier>(
            PercentTieredEvChargeProposalAdjuster(5.4),
            UnitTieredEvChargeProposalAdjuster(
                mutableMapOf<String, Double>("east" to 0.95, "north" to 0.85, "retail" to 0.82),
            ),
            TieredEvChargeProposalQuantityOffsetAdjuster(1.94),
            CapTieredEvChargeProposalAdjuster(20980.0),
            RoundingTieredEvChargeProposalAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 54529.2,
    )
}
