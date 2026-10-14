package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer review calculation. */
interface TrailerReviewModifier {
    fun describe(): String
    fun adjust(item: TrailerReview, value: Double): Double
}

data class PercentTrailerReviewAdjuster(var percent: Double) : TrailerReviewModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTrailerReviewAdjuster(var step: Double) : TrailerReviewModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerReview, value: Double): Double = roundTo(value, step)
}

data class CapTrailerReviewAdjuster(var cap: Double) : TrailerReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerReview, value: Double): Double = minOf(value, cap)
}

data class FlaggedTrailerReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameTrailerReviewAdjuster(var factors: MutableMap<String, Double>) : TrailerReviewModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: TrailerReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TrailerReviewRevisionOffsetAdjuster(var weight: Double) : TrailerReviewModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: TrailerReview, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the trailer review steps in order and keeps a trace of each. */
data class TrailerReviewEngine(
    var steps: MutableList<TrailerReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerReview): Double {
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

    fun evaluateAll(items: MutableList<TrailerReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerReviewEngine(flags: Registry): TrailerReviewEngine {
    return TrailerReviewEngine(
        steps = mutableListOf<TrailerReviewModifier>(
            PercentTrailerReviewAdjuster(13.1),
            RoundingTrailerReviewAdjuster(0.25),
            CapTrailerReviewAdjuster(30740.0),
            FlaggedTrailerReviewAdjuster(flags, "trailer-review-fallback", -8.5),
            NameTrailerReviewAdjuster(mutableMapOf<String, Double>("west" to 1.07, "retail" to 0.71)),
            TrailerReviewRevisionOffsetAdjuster(0.66),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 73076.9,
    )
}
