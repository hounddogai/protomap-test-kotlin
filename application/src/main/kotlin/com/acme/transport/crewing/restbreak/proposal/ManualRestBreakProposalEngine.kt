package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual rest break proposal calculation. */
interface ManualRestBreakProposalAdjuster {
    fun describe(): String
    fun adjust(item: ManualRestBreakProposal, value: Double): Double
}

data class FlaggedManualRestBreakProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRestBreakProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRestBreakProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualRestBreakProposalAdjuster(var cap: Double) : ManualRestBreakProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualRestBreakProposal, value: Double): Double = minOf(value, cap)
}

data class TitleManualRestBreakProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualRestBreakProposalAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualRestBreakProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualRestBreakProposalAdjuster(var percent: Double) : ManualRestBreakProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualRestBreakProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualRestBreakProposalAdjuster(var minimum: Double) : ManualRestBreakProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRestBreakProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualRestBreakProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRestBreakProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRestBreakProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual rest break proposal steps in order and keeps a trace of each. */
data class ManualRestBreakProposalEngine(
    var steps: MutableList<ManualRestBreakProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualRestBreakProposal): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualRestBreakProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualRestBreakProposalEngine(flags: Registry): ManualRestBreakProposalEngine {
    return ManualRestBreakProposalEngine(
        steps = mutableListOf<ManualRestBreakProposalAdjuster>(
            FlaggedManualRestBreakProposalAdjuster(flags, "manual-rest-break-proposal-fallback", -4.6),
            CapManualRestBreakProposalAdjuster(45487.0),
            TitleManualRestBreakProposalAdjuster(
                mutableMapOf<String, Double>("north" to 1.09, "retail" to 0.74, "west" to 1.11),
            ),
            PercentManualRestBreakProposalAdjuster(11.8),
            MinimumManualRestBreakProposalAdjuster(3.9),
            TieredManualRestBreakProposalAdjuster(
                mutableListOf<Double>(161.0, 9182.0),
                mutableListOf<Double>(1.18, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 60339.1,
    )
}
