package com.acme.transport.linehaul.tender.tier

/** One step of the adaptive tender proposal calculation. */
interface AdaptiveTenderProposalFactor {
    fun describe(): String
    fun adjust(item: AdaptiveTenderProposal, value: Double): Double
}

data class ChannelAdaptiveTenderProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveTenderProposalFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: AdaptiveTenderProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveTenderProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveTenderProposalFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveTenderProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAdaptiveTenderProposalAdjuster(var percent: Double) : AdaptiveTenderProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTenderProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveTenderProposalMinimumOffsetAdjuster(var weight: Double) : AdaptiveTenderProposalFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: AdaptiveTenderProposal, value: Double): Double = value + item.minimum * weight
}

data class CapAdaptiveTenderProposalAdjuster(var cap: Double) : AdaptiveTenderProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTenderProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive tender proposal steps in order and keeps a trace of each. */
data class AdaptiveTenderProposalEngine(
    var steps: MutableList<AdaptiveTenderProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveTenderProposal): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveTenderProposalFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveTenderProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveTenderProposalEngine(): AdaptiveTenderProposalEngine {
    return AdaptiveTenderProposalEngine(
        steps = mutableListOf<AdaptiveTenderProposalFactor>(
            ChannelAdaptiveTenderProposalAdjuster(mutableMapOf<String, Double>("west" to 1.17, "north" to 1.36)),
            TieredAdaptiveTenderProposalAdjuster(
                mutableListOf<Double>(6383.0, 7554.0, 8390.0),
                mutableListOf<Double>(1.01, 0.83, 1.16),
            ),
            PercentAdaptiveTenderProposalAdjuster(-16.7),
            AdaptiveTenderProposalMinimumOffsetAdjuster(0.57),
            CapAdaptiveTenderProposalAdjuster(41162.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 2473.3,
    )
}
