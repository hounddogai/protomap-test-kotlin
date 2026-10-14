package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock review calculation. */
interface CrossDockReviewStep {
    fun describe(): String
    fun adjust(item: CrossDockReview, value: Double): Double
}

data class MinimumCrossDockReviewAdjuster(var minimum: Double) : CrossDockReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCrossDockReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCrossDockReviewAdjuster(var cap: Double) : CrossDockReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockReview, value: Double): Double = minOf(value, cap)
}

data class RoundingCrossDockReviewAdjuster(var step: Double) : CrossDockReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockReview, value: Double): Double = roundTo(value, step)
}

/** Runs the cross dock review steps in order and keeps a trace of each. */
data class CrossDockReviewEngine(
    var steps: MutableList<CrossDockReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockReview): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockReviewEngine(flags: Registry): CrossDockReviewEngine {
    return CrossDockReviewEngine(
        steps = mutableListOf<CrossDockReviewStep>(
            MinimumCrossDockReviewAdjuster(13.0),
            FlaggedCrossDockReviewAdjuster(flags, "cross-dock-review-strict", -9.0),
            CapCrossDockReviewAdjuster(15589.0),
            RoundingCrossDockReviewAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 16719.3,
    )
}
