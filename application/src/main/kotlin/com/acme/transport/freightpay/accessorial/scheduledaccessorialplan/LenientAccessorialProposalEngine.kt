package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient accessorial proposal calculation. */
interface LenientAccessorialProposalModifier {
    fun describe(): String
    fun adjust(item: LenientAccessorialProposal, value: Double): Double
}

data class PercentLenientAccessorialProposalAdjuster(var percent: Double) : LenientAccessorialProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAccessorialProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientAccessorialProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAccessorialProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAccessorialProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLenientAccessorialProposalAdjuster(var step: Double) : LenientAccessorialProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAccessorialProposal, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientAccessorialProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAccessorialProposalModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAccessorialProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient accessorial proposal steps in order and keeps a trace of each. */
data class LenientAccessorialProposalEngine(
    var steps: MutableList<LenientAccessorialProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientAccessorialProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientAccessorialProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientAccessorialProposalEngine(flags: Registry): LenientAccessorialProposalEngine {
    return LenientAccessorialProposalEngine(
        steps = mutableListOf<LenientAccessorialProposalModifier>(
            PercentLenientAccessorialProposalAdjuster(12.4),
            TieredLenientAccessorialProposalAdjuster(
                mutableListOf<Double>(9329.0, 9741.0),
                mutableListOf<Double>(1.14, 1.03),
            ),
            RoundingLenientAccessorialProposalAdjuster(0.5),
            FlaggedLenientAccessorialProposalAdjuster(flags, "lenient-accessorial-proposal-strict", 0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 56428.1,
    )
}
