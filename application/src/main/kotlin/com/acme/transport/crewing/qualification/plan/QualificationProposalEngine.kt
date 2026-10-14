package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification proposal calculation. */
interface QualificationProposalAdjuster {
    fun describe(): String
    fun adjust(item: QualificationProposal, value: Double): Double
}

data class CapQualificationProposalAdjuster(var cap: Double) : QualificationProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationProposal, value: Double): Double = minOf(value, cap)
}

data class QualificationProposalAttemptsOffsetAdjuster(var weight: Double) : QualificationProposalAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: QualificationProposal, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class LaneQualificationProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationProposalAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: QualificationProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingQualificationProposalAdjuster(var step: Double) : QualificationProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationProposal, value: Double): Double = roundTo(value, step)
}

data class TieredQualificationProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedQualificationProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the qualification proposal steps in order and keeps a trace of each. */
data class QualificationProposalEngine(
    var steps: MutableList<QualificationProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationProposal): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationProposalEngine(flags: Registry): QualificationProposalEngine {
    return QualificationProposalEngine(
        steps = mutableListOf<QualificationProposalAdjuster>(
            CapQualificationProposalAdjuster(2649.0),
            QualificationProposalAttemptsOffsetAdjuster(1.29),
            LaneQualificationProposalAdjuster(mutableMapOf<String, Double>("south" to 1.17)),
            RoundingQualificationProposalAdjuster(5.0),
            TieredQualificationProposalAdjuster(
                mutableListOf<Double>(3332.0, 7510.0, 8853.0),
                mutableListOf<Double>(0.95, 1.16, 0.87),
            ),
            FlaggedQualificationProposalAdjuster(flags, "qualification-proposal-global", -1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 87393.7,
    )
}
