package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dispute summary calculation. */
interface DisputeSummaryStep {
    fun describe(): String
    fun adjust(item: DisputeSummary, value: Double): Double
}

data class FlaggedDisputeSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDisputeSummaryAdjuster(var step: Double) : DisputeSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeSummary, value: Double): Double = roundTo(value, step)
}

data class PercentDisputeSummaryAdjuster(var percent: Double) : DisputeSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelDisputeSummaryAdjuster(var factors: MutableMap<String, Double>) : DisputeSummaryStep {
    override fun describe(): String = "by label"

    override fun adjust(item: DisputeSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDisputeSummaryAdjuster(var cap: Double) : DisputeSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumDisputeSummaryAdjuster(var minimum: Double) : DisputeSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dispute summary steps in order and keeps a trace of each. */
data class DisputeSummaryEngine(
    var steps: MutableList<DisputeSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DisputeSummary): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DisputeSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDisputeSummaryEngine(flags: Registry): DisputeSummaryEngine {
    return DisputeSummaryEngine(
        steps = mutableListOf<DisputeSummaryStep>(
            FlaggedDisputeSummaryAdjuster(flags, "dispute-summary-default", 5.8),
            RoundingDisputeSummaryAdjuster(0.1),
            PercentDisputeSummaryAdjuster(3.3),
            LabelDisputeSummaryAdjuster(mutableMapOf<String, Double>("east" to 1.3, "north" to 1.14)),
            CapDisputeSummaryAdjuster(28644.0),
            MinimumDisputeSummaryAdjuster(15.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 53581.9,
    )
}
