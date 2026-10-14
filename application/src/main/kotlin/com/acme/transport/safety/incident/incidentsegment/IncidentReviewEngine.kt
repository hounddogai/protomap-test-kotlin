package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.flags.Registry

/** One step of the incident review calculation. */
interface IncidentReviewAdjuster {
    fun describe(): String
    fun adjust(item: IncidentReview, value: Double): Double
}

data class MinimumIncidentReviewAdjuster(var minimum: Double) : IncidentReviewAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedIncidentReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentIncidentReviewAdjuster(var percent: Double) : IncidentReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class IncidentReviewEndsAtOffsetAdjuster(var weight: Double) : IncidentReviewAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: IncidentReview, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the incident review steps in order and keeps a trace of each. */
data class IncidentReviewEngine(
    var steps: MutableList<IncidentReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentReview): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentReviewEngine(flags: Registry): IncidentReviewEngine {
    return IncidentReviewEngine(
        steps = mutableListOf<IncidentReviewAdjuster>(
            MinimumIncidentReviewAdjuster(33.0),
            FlaggedIncidentReviewAdjuster(flags, "incident-review-tiered", 3.0),
            PercentIncidentReviewAdjuster(-22.6),
            IncidentReviewEndsAtOffsetAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 51789.1,
    )
}
