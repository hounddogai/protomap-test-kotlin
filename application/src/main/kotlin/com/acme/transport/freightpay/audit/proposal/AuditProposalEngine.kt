package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the audit proposal calculation. */
interface AuditProposalAdjuster {
    fun describe(): String
    fun adjust(item: AuditProposal, value: Double): Double
}

data class CategoryAuditProposalAdjuster(var factors: MutableMap<String, Double>) : AuditProposalAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: AuditProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAuditProposalAdjuster(var cap: Double) : AuditProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditProposal, value: Double): Double = minOf(value, cap)
}

data class MinimumAuditProposalAdjuster(var minimum: Double) : AuditProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAuditProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAuditProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AuditProposalRetriesOffsetAdjuster(var weight: Double) : AuditProposalAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: AuditProposal, value: Double): Double = value + item.retries.toDouble() * weight
}

/** Runs the audit proposal steps in order and keeps a trace of each. */
data class AuditProposalEngine(
    var steps: MutableList<AuditProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditProposal): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditProposalEngine(flags: Registry): AuditProposalEngine {
    return AuditProposalEngine(
        steps = mutableListOf<AuditProposalAdjuster>(
            CategoryAuditProposalAdjuster(mutableMapOf<String, Double>("online" to 1.32)),
            CapAuditProposalAdjuster(16090.0),
            MinimumAuditProposalAdjuster(38.0),
            TieredAuditProposalAdjuster(
                mutableListOf<Double>(472.0, 6324.0, 9308.0),
                mutableListOf<Double>(0.92, 0.81, 1.01),
            ),
            FlaggedAuditProposalAdjuster(flags, "audit-proposal-rolling", 9.9),
            AuditProposalRetriesOffsetAdjuster(0.72),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 42300.0,
    )
}
