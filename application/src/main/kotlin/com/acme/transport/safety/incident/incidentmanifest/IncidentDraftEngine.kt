package com.acme.transport.safety.incident.incidentmanifest

/** One step of the incident draft calculation. */
interface IncidentDraftStep {
    fun describe(): String
    fun adjust(item: IncidentDraft, value: Double): Double
}

data class MinimumIncidentDraftAdjuster(var minimum: Double) : IncidentDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentIncidentDraftAdjuster(var percent: Double) : IncidentDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelIncidentDraftAdjuster(var factors: MutableMap<String, Double>) : IncidentDraftStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: IncidentDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredIncidentDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapIncidentDraftAdjuster(var cap: Double) : IncidentDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the incident draft steps in order and keeps a trace of each. */
data class IncidentDraftEngine(
    var steps: MutableList<IncidentDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentDraftEngine(): IncidentDraftEngine {
    return IncidentDraftEngine(
        steps = mutableListOf<IncidentDraftStep>(
            MinimumIncidentDraftAdjuster(22.8),
            PercentIncidentDraftAdjuster(17.3),
            ChannelIncidentDraftAdjuster(
                mutableMapOf<String, Double>("central" to 1.01, "east" to 0.93, "north" to 1.17),
            ),
            TieredIncidentDraftAdjuster(
                mutableListOf<Double>(2387.0, 7939.0),
                mutableListOf<Double>(1.14, 0.91),
            ),
            CapIncidentDraftAdjuster(939.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 88575.7,
    )
}
