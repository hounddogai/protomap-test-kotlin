package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict zone summary calculation. */
interface StrictZoneSummaryStep {
    fun describe(): String
    fun adjust(item: StrictZoneSummary, value: Double): Double
}

data class StrictZoneSummaryCostOffsetAdjuster(var weight: Double) : StrictZoneSummaryStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: StrictZoneSummary, value: Double): Double = value + item.cost * weight
}

data class CapStrictZoneSummaryAdjuster(var cap: Double) : StrictZoneSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictZoneSummary, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictZoneSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictZoneSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictZoneSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictZoneSummaryAdjuster(var percent: Double) : StrictZoneSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictZoneSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingStrictZoneSummaryAdjuster(var step: Double) : StrictZoneSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictZoneSummary, value: Double): Double = roundTo(value, step)
}

/** Runs the strict zone summary steps in order and keeps a trace of each. */
data class StrictZoneSummaryEngine(
    var steps: MutableList<StrictZoneSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictZoneSummary): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictZoneSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictZoneSummaryEngine(flags: Registry): StrictZoneSummaryEngine {
    return StrictZoneSummaryEngine(
        steps = mutableListOf<StrictZoneSummaryStep>(
            StrictZoneSummaryCostOffsetAdjuster(0.64),
            CapStrictZoneSummaryAdjuster(30471.0),
            FlaggedStrictZoneSummaryAdjuster(flags, "strict-zone-summary-capped", -5.5),
            PercentStrictZoneSummaryAdjuster(-22.2),
            RoundingStrictZoneSummaryAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 94927.5,
    )
}
