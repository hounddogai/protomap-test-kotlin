package com.acme.transport.safety.incident.claim

/** One step of the regional incident review calculation. */
interface RegionalIncidentReviewModifier {
    fun describe(): String
    fun adjust(item: RegionalIncidentReview, value: Double): Double
}

data class MinimumRegionalIncidentReviewAdjuster(var minimum: Double) : RegionalIncidentReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalIncidentReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalIncidentReviewStartsAtOffsetAdjuster(var weight: Double) : RegionalIncidentReviewModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RegionalIncidentReview, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class NameRegionalIncidentReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalIncidentReviewModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalIncidentReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional incident review steps in order and keeps a trace of each. */
data class RegionalIncidentReviewEngine(
    var steps: MutableList<RegionalIncidentReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalIncidentReview): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalIncidentReviewModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalIncidentReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalIncidentReviewEngine(): RegionalIncidentReviewEngine {
    return RegionalIncidentReviewEngine(
        steps = mutableListOf<RegionalIncidentReviewModifier>(
            MinimumRegionalIncidentReviewAdjuster(24.3),
            RegionalIncidentReviewStartsAtOffsetAdjuster(1.63),
            NameRegionalIncidentReviewAdjuster(
                mutableMapOf<String, Double>("online" to 0.75, "east" to 0.83, "south" to 0.72),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 39405.5,
    )
}
