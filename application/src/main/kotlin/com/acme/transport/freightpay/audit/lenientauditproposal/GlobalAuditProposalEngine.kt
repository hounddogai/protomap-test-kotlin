package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global audit proposal calculation. */
interface GlobalAuditProposalFactor {
    fun describe(): String
    fun adjust(item: GlobalAuditProposal, value: Double): Double
}

data class PercentGlobalAuditProposalAdjuster(var percent: Double) : GlobalAuditProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalAuditProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingGlobalAuditProposalAdjuster(var step: Double) : GlobalAuditProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAuditProposal, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalAuditProposalAdjuster(var minimum: Double) : GlobalAuditProposalFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalAuditProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalAuditProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAuditProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAuditProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalAuditProposalCreatedAtOffsetAdjuster(var weight: Double) : GlobalAuditProposalFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: GlobalAuditProposal, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class TieredGlobalAuditProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalAuditProposalFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalAuditProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global audit proposal steps in order and keeps a trace of each. */
data class GlobalAuditProposalEngine(
    var steps: MutableList<GlobalAuditProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalAuditProposal): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalAuditProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalAuditProposalEngine(flags: Registry): GlobalAuditProposalEngine {
    return GlobalAuditProposalEngine(
        steps = mutableListOf<GlobalAuditProposalFactor>(
            PercentGlobalAuditProposalAdjuster(19.2),
            RoundingGlobalAuditProposalAdjuster(0.1),
            MinimumGlobalAuditProposalAdjuster(5.6),
            FlaggedGlobalAuditProposalAdjuster(flags, "global-audit-proposal-lenient", -9.7),
            GlobalAuditProposalCreatedAtOffsetAdjuster(1.39),
            TieredGlobalAuditProposalAdjuster(
                mutableListOf<Double>(536.0, 850.0, 1011.0, 9219.0),
                mutableListOf<Double>(0.88, 0.82, 1.03, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 57785.5,
    )
}
