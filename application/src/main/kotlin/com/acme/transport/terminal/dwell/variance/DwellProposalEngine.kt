package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell proposal calculation. */
interface DwellProposalStep {
    fun describe(): String
    fun adjust(item: DwellProposal, value: Double): Double
}

data class FlaggedDwellProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DwellProposalAmountOffsetAdjuster(var weight: Double) : DwellProposalStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: DwellProposal, value: Double): Double = value + item.amount * weight
}

data class TieredDwellProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDwellProposalAdjuster(var step: Double) : DwellProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellProposal, value: Double): Double = roundTo(value, step)
}

data class MinimumDwellProposalAdjuster(var minimum: Double) : DwellProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleDwellProposalAdjuster(var factors: MutableMap<String, Double>) : DwellProposalStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DwellProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dwell proposal steps in order and keeps a trace of each. */
data class DwellProposalEngine(
    var steps: MutableList<DwellProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DwellProposal): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDwellProposalEngine(flags: Registry): DwellProposalEngine {
    return DwellProposalEngine(
        steps = mutableListOf<DwellProposalStep>(
            FlaggedDwellProposalAdjuster(flags, "dwell-proposal-tiered", -4.7),
            DwellProposalAmountOffsetAdjuster(0.9),
            TieredDwellProposalAdjuster(
                mutableListOf<Double>(901.0, 2650.0, 4218.0),
                mutableListOf<Double>(0.91, 1.09, 0.82),
            ),
            RoundingDwellProposalAdjuster(0.1),
            MinimumDwellProposalAdjuster(17.3),
            TitleDwellProposalAdjuster(mutableMapOf<String, Double>("retail" to 1.3, "east" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 6312.1,
    )
}
