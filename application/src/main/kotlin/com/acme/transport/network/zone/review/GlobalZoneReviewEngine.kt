package com.acme.transport.network.zone.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global zone review calculation. */
interface GlobalZoneReviewModifier {
    fun describe(): String
    fun adjust(item: GlobalZoneReview, value: Double): Double
}

data class CapGlobalZoneReviewAdjuster(var cap: Double) : GlobalZoneReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalZoneReview, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalZoneReviewAdjuster(var minimum: Double) : GlobalZoneReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalZoneReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryGlobalZoneReviewAdjuster(var factors: MutableMap<String, Double>) : GlobalZoneReviewModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: GlobalZoneReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalZoneReviewAdjuster(var percent: Double) : GlobalZoneReviewModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalZoneReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalZoneReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalZoneReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalZoneReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalZoneReviewAdjuster(var step: Double) : GlobalZoneReviewModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalZoneReview, value: Double): Double = roundTo(value, step)
}

/** Runs the global zone review steps in order and keeps a trace of each. */
data class GlobalZoneReviewEngine(
    var steps: MutableList<GlobalZoneReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalZoneReview): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalZoneReviewModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalZoneReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalZoneReviewEngine(flags: Registry): GlobalZoneReviewEngine {
    return GlobalZoneReviewEngine(
        steps = mutableListOf<GlobalZoneReviewModifier>(
            CapGlobalZoneReviewAdjuster(10197.0),
            MinimumGlobalZoneReviewAdjuster(27.8),
            CountryGlobalZoneReviewAdjuster(
                mutableMapOf<String, Double>("east" to 1.29, "online" to 1.31, "retail" to 1.21),
            ),
            PercentGlobalZoneReviewAdjuster(-20.2),
            FlaggedGlobalZoneReviewAdjuster(flags, "global-zone-review-global", -4.1),
            RoundingGlobalZoneReviewAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 97135.0,
    )
}
