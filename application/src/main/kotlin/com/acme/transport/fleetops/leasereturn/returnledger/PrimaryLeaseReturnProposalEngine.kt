package com.acme.transport.fleetops.leasereturn.returnledger

/** One step of the primary lease return proposal calculation. */
interface PrimaryLeaseReturnProposalAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryLeaseReturnProposal, value: Double): Double
}

data class PercentPrimaryLeaseReturnProposalAdjuster(var percent: Double) : PrimaryLeaseReturnProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryLeaseReturnProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryLeaseReturnProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryLeaseReturnProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryLeaseReturnProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryLeaseReturnProposalAdjuster(var minimum: Double) : PrimaryLeaseReturnProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryLeaseReturnProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryLeaseReturnProposalSequenceOffsetAdjuster(
    var weight: Double,
) : PrimaryLeaseReturnProposalAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: PrimaryLeaseReturnProposal, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class CapPrimaryLeaseReturnProposalAdjuster(var cap: Double) : PrimaryLeaseReturnProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryLeaseReturnProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the primary lease return proposal steps in order and keeps a trace of each. */
data class PrimaryLeaseReturnProposalEngine(
    var steps: MutableList<PrimaryLeaseReturnProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryLeaseReturnProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryLeaseReturnProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryLeaseReturnProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryLeaseReturnProposalEngine(): PrimaryLeaseReturnProposalEngine {
    return PrimaryLeaseReturnProposalEngine(
        steps = mutableListOf<PrimaryLeaseReturnProposalAdjuster>(
            PercentPrimaryLeaseReturnProposalAdjuster(-23.4),
            TieredPrimaryLeaseReturnProposalAdjuster(
                mutableListOf<Double>(3944.0, 8559.0),
                mutableListOf<Double>(0.83, 1.16),
            ),
            MinimumPrimaryLeaseReturnProposalAdjuster(16.7),
            PrimaryLeaseReturnProposalSequenceOffsetAdjuster(0.09),
            CapPrimaryLeaseReturnProposalAdjuster(36615.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 50196.8,
    )
}
