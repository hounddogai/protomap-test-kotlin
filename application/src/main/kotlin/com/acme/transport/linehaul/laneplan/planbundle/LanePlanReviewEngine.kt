package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan review calculation. */
interface LanePlanReviewFactor {
    fun describe(): String
    fun adjust(item: LanePlanReview, value: Double): Double
}

data class TieredLanePlanReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLanePlanReviewAdjuster(var minimum: Double) : LanePlanReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLanePlanReviewAdjuster(var step: Double) : LanePlanReviewFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanReview, value: Double): Double = roundTo(value, step)
}

data class FlaggedLanePlanReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLanePlanReviewAdjuster(var cap: Double) : LanePlanReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanReview, value: Double): Double = minOf(value, cap)
}

/** Runs the lane plan review steps in order and keeps a trace of each. */
data class LanePlanReviewEngine(
    var steps: MutableList<LanePlanReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanReview): Double {
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

    fun evaluateAll(items: MutableList<LanePlanReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanReviewEngine(flags: Registry): LanePlanReviewEngine {
    return LanePlanReviewEngine(
        steps = mutableListOf<LanePlanReviewFactor>(
            TieredLanePlanReviewAdjuster(
                mutableListOf<Double>(490.0, 2741.0, 3251.0, 7528.0),
                mutableListOf<Double>(1.16, 1.16, 1.0, 1.02),
            ),
            MinimumLanePlanReviewAdjuster(34.4),
            RoundingLanePlanReviewAdjuster(0.1),
            FlaggedLanePlanReviewAdjuster(flags, "lane-plan-review-pending", 3.9),
            CapLanePlanReviewAdjuster(42769.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 22821.3,
    )
}
