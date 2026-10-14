package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional trailer yard summary calculation. */
interface RegionalTrailerYardSummaryFactor {
    fun describe(): String
    fun adjust(item: RegionalTrailerYardSummary, value: Double): Double
}

data class RoundingRegionalTrailerYardSummaryAdjuster(var step: Double) : RegionalTrailerYardSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalTrailerYardSummary, value: Double): Double = roundTo(value, step)
}

data class RegionalTrailerYardSummaryMarginOffsetAdjuster(
    var weight: Double,
) : RegionalTrailerYardSummaryFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: RegionalTrailerYardSummary, value: Double): Double = value + item.margin * weight
}

data class PercentRegionalTrailerYardSummaryAdjuster(var percent: Double) : RegionalTrailerYardSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTrailerYardSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional trailer yard summary steps in order and keeps a trace of each. */
data class RegionalTrailerYardSummaryEngine(
    var steps: MutableList<RegionalTrailerYardSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalTrailerYardSummary): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalTrailerYardSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalTrailerYardSummaryEngine(): RegionalTrailerYardSummaryEngine {
    return RegionalTrailerYardSummaryEngine(
        steps = mutableListOf<RegionalTrailerYardSummaryFactor>(
            RoundingRegionalTrailerYardSummaryAdjuster(1.0),
            RegionalTrailerYardSummaryMarginOffsetAdjuster(1.69),
            PercentRegionalTrailerYardSummaryAdjuster(-8.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 48427.1,
    )
}
