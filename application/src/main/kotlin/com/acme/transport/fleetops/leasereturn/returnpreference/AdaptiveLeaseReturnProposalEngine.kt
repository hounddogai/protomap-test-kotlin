package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive lease return proposal calculation. */
interface AdaptiveLeaseReturnProposalAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveLeaseReturnProposal, value: Double): Double
}

data class AdaptiveLeaseReturnProposalFuelLitersOffsetAdjuster(
    var weight: Double,
) : AdaptiveLeaseReturnProposalAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: AdaptiveLeaseReturnProposal, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedAdaptiveLeaseReturnProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLeaseReturnProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLeaseReturnProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveLeaseReturnProposalAdjuster(
    var percent: Double,
) : AdaptiveLeaseReturnProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveLeaseReturnProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentAdaptiveLeaseReturnProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLeaseReturnProposalAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: AdaptiveLeaseReturnProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive lease return proposal steps in order and keeps a trace of each. */
data class AdaptiveLeaseReturnProposalEngine(
    var steps: MutableList<AdaptiveLeaseReturnProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveLeaseReturnProposal): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveLeaseReturnProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveLeaseReturnProposalEngine(flags: Registry): AdaptiveLeaseReturnProposalEngine {
    return AdaptiveLeaseReturnProposalEngine(
        steps = mutableListOf<AdaptiveLeaseReturnProposalAdjuster>(
            AdaptiveLeaseReturnProposalFuelLitersOffsetAdjuster(0.99),
            FlaggedAdaptiveLeaseReturnProposalAdjuster(flags, "adaptive-lease-return-proposal-scheduled", -0.6),
            PercentAdaptiveLeaseReturnProposalAdjuster(-11.1),
            SegmentAdaptiveLeaseReturnProposalAdjuster(
                mutableMapOf<String, Double>("retail" to 0.92, "west" to 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 38722.1,
    )
}
