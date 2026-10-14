package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.flags.Registry

/** One step of the roster mapping calculation. */
interface RosterMappingStep {
    fun describe(): String
    fun adjust(item: RosterMapping, value: Double): Double
}

data class FlaggedRosterMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelRosterMappingAdjuster(var factors: MutableMap<String, Double>) : RosterMappingStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RosterMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRosterMappingAdjuster(var minimum: Double) : RosterMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRosterMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the roster mapping steps in order and keeps a trace of each. */
data class RosterMappingEngine(
    var steps: MutableList<RosterMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<RosterMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterMappingEngine(flags: Registry): RosterMappingEngine {
    return RosterMappingEngine(
        steps = mutableListOf<RosterMappingStep>(
            FlaggedRosterMappingAdjuster(flags, "roster-mapping-batched", 5.5),
            LabelRosterMappingAdjuster(
                mutableMapOf<String, Double>("central" to 0.72, "retail" to 0.85, "north" to 1.35),
            ),
            MinimumRosterMappingAdjuster(34.4),
            TieredRosterMappingAdjuster(
                mutableListOf<Double>(2443.0, 7820.0, 9609.0),
                mutableListOf<Double>(0.83, 0.85, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 37572.6,
    )
}
