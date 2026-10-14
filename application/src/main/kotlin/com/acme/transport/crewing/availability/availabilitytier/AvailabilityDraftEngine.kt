package com.acme.transport.crewing.availability.availabilitytier

/** One step of the availability draft calculation. */
interface AvailabilityDraftStep {
    fun describe(): String
    fun adjust(item: AvailabilityDraft, value: Double): Double
}

data class CapAvailabilityDraftAdjuster(var cap: Double) : AvailabilityDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityDraft, value: Double): Double = minOf(value, cap)
}

data class SourceAvailabilityDraftAdjuster(var factors: MutableMap<String, Double>) : AvailabilityDraftStep {
    override fun describe(): String = "by source"

    override fun adjust(item: AvailabilityDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAvailabilityDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAvailabilityDraftAdjuster(var minimum: Double) : AvailabilityDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the availability draft steps in order and keeps a trace of each. */
data class AvailabilityDraftEngine(
    var steps: MutableList<AvailabilityDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AvailabilityDraft): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAvailabilityDraftEngine(): AvailabilityDraftEngine {
    return AvailabilityDraftEngine(
        steps = mutableListOf<AvailabilityDraftStep>(
            CapAvailabilityDraftAdjuster(18703.0),
            SourceAvailabilityDraftAdjuster(
                mutableMapOf<String, Double>("north" to 0.71, "retail" to 1.34, "east" to 0.9),
            ),
            TieredAvailabilityDraftAdjuster(
                mutableListOf<Double>(3028.0, 8017.0),
                mutableListOf<Double>(0.88, 1.09),
            ),
            MinimumAvailabilityDraftAdjuster(37.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 54341.9,
    )
}
