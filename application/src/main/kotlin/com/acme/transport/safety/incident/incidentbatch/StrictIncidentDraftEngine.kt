package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict incident draft calculation. */
interface StrictIncidentDraftFactor {
    fun describe(): String
    fun adjust(item: StrictIncidentDraft, value: Double): Double
}

data class TieredStrictIncidentDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictIncidentDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictIncidentDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictIncidentDraftAdjuster(var minimum: Double) : StrictIncidentDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictIncidentDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictIncidentDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictIncidentDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictIncidentDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictIncidentDraftAdjuster(var step: Double) : StrictIncidentDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictIncidentDraft, value: Double): Double = roundTo(value, step)
}

data class DepotStrictIncidentDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictIncidentDraftFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: StrictIncidentDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class StrictIncidentDraftScoreOffsetAdjuster(var weight: Double) : StrictIncidentDraftFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: StrictIncidentDraft, value: Double): Double = value + item.score * weight
}

/** Runs the strict incident draft steps in order and keeps a trace of each. */
data class StrictIncidentDraftEngine(
    var steps: MutableList<StrictIncidentDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictIncidentDraft): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictIncidentDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictIncidentDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictIncidentDraftEngine(flags: Registry): StrictIncidentDraftEngine {
    return StrictIncidentDraftEngine(
        steps = mutableListOf<StrictIncidentDraftFactor>(
            TieredStrictIncidentDraftAdjuster(
                mutableListOf<Double>(3612.0, 4423.0, 8053.0),
                mutableListOf<Double>(0.96, 1.05, 0.99),
            ),
            MinimumStrictIncidentDraftAdjuster(23.4),
            FlaggedStrictIncidentDraftAdjuster(flags, "strict-incident-draft-strict", 6.1),
            RoundingStrictIncidentDraftAdjuster(0.5),
            DepotStrictIncidentDraftAdjuster(mutableMapOf<String, Double>("online" to 1.33)),
            StrictIncidentDraftScoreOffsetAdjuster(1.38),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 78710.2,
    )
}
