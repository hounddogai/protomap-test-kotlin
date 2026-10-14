package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient duty period proposal calculation. */
interface LenientDutyPeriodProposalFactor {
    fun describe(): String
    fun adjust(item: LenientDutyPeriodProposal, value: Double): Double
}

data class CapLenientDutyPeriodProposalAdjuster(var cap: Double) : LenientDutyPeriodProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDutyPeriodProposal, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientDutyPeriodProposalAdjuster(var step: Double) : LenientDutyPeriodProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDutyPeriodProposal, value: Double): Double = roundTo(value, step)
}

data class CountryLenientDutyPeriodProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDutyPeriodProposalFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientDutyPeriodProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LenientDutyPeriodProposalEndsAtOffsetAdjuster(var weight: Double) : LenientDutyPeriodProposalFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: LenientDutyPeriodProposal, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

/** Runs the lenient duty period proposal steps in order and keeps a trace of each. */
data class LenientDutyPeriodProposalEngine(
    var steps: MutableList<LenientDutyPeriodProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientDutyPeriodProposal): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDutyPeriodProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientDutyPeriodProposalEngine(): LenientDutyPeriodProposalEngine {
    return LenientDutyPeriodProposalEngine(
        steps = mutableListOf<LenientDutyPeriodProposalFactor>(
            CapLenientDutyPeriodProposalAdjuster(14198.0),
            RoundingLenientDutyPeriodProposalAdjuster(0.25),
            CountryLenientDutyPeriodProposalAdjuster(
                mutableMapOf<String, Double>("west" to 0.73, "north" to 1.3, "online" to 1.37),
            ),
            LenientDutyPeriodProposalEndsAtOffsetAdjuster(1.28),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 19699.6,
    )
}
