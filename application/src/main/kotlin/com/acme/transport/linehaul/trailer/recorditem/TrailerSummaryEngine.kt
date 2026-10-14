package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer summary calculation. */
interface TrailerSummaryStep {
    fun describe(): String
    fun adjust(item: TrailerSummary, value: Double): Double
}

data class TrailerSummaryScoreOffsetAdjuster(var weight: Double) : TrailerSummaryStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TrailerSummary, value: Double): Double = value + item.score * weight
}

data class FlaggedTrailerSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTrailerSummaryAdjuster(var percent: Double) : TrailerSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTrailerSummaryAdjuster(var minimum: Double) : TrailerSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTrailerSummaryAdjuster(var step: Double) : TrailerSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerSummary, value: Double): Double = roundTo(value, step)
}

data class LaneTrailerSummaryAdjuster(var factors: MutableMap<String, Double>) : TrailerSummaryStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: TrailerSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer summary steps in order and keeps a trace of each. */
data class TrailerSummaryEngine(
    var steps: MutableList<TrailerSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerSummary): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerSummaryEngine(flags: Registry): TrailerSummaryEngine {
    return TrailerSummaryEngine(
        steps = mutableListOf<TrailerSummaryStep>(
            TrailerSummaryScoreOffsetAdjuster(1.52),
            FlaggedTrailerSummaryAdjuster(flags, "trailer-summary-seasonal", 4.2),
            PercentTrailerSummaryAdjuster(-19.1),
            MinimumTrailerSummaryAdjuster(44.8),
            RoundingTrailerSummaryAdjuster(0.05),
            LaneTrailerSummaryAdjuster(mutableMapOf<String, Double>("south" to 1.28)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 26657.2,
    )
}
