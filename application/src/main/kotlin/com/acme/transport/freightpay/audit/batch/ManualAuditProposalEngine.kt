package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.flags.Registry

/** One step of the manual audit proposal calculation. */
interface ManualAuditProposalModifier {
    fun describe(): String
    fun adjust(item: ManualAuditProposal, value: Double): Double
}

data class ManualAuditProposalLoadKgOffsetAdjuster(var weight: Double) : ManualAuditProposalModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ManualAuditProposal, value: Double): Double = value + item.loadKg * weight
}

data class TitleManualAuditProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualAuditProposalModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualAuditProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualAuditProposalAdjuster(var minimum: Double) : ManualAuditProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAuditProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualAuditProposalAdjuster(var percent: Double) : ManualAuditProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAuditProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualAuditProposalAdjuster(var cap: Double) : ManualAuditProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAuditProposal, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualAuditProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAuditProposalModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAuditProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual audit proposal steps in order and keeps a trace of each. */
data class ManualAuditProposalEngine(
    var steps: MutableList<ManualAuditProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualAuditProposal): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualAuditProposalModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualAuditProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualAuditProposalEngine(flags: Registry): ManualAuditProposalEngine {
    return ManualAuditProposalEngine(
        steps = mutableListOf<ManualAuditProposalModifier>(
            ManualAuditProposalLoadKgOffsetAdjuster(0.49),
            TitleManualAuditProposalAdjuster(
                mutableMapOf<String, Double>("online" to 1.39, "west" to 1.01, "retail" to 0.87),
            ),
            MinimumManualAuditProposalAdjuster(6.8),
            PercentManualAuditProposalAdjuster(1.0),
            CapManualAuditProposalAdjuster(1518.0),
            FlaggedManualAuditProposalAdjuster(flags, "manual-audit-proposal-default", 5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 45896.4,
    )
}
