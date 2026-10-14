package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.flags.Registry

/** One step of the strict roster mapping calculation. */
interface StrictRosterMappingModifier {
    fun describe(): String
    fun adjust(item: StrictRosterMapping, value: Double): Double
}

data class CapStrictRosterMappingAdjuster(var cap: Double) : StrictRosterMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRosterMapping, value: Double): Double = minOf(value, cap)
}

data class MinimumStrictRosterMappingAdjuster(var minimum: Double) : StrictRosterMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRosterMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictRosterMappingThresholdOffsetAdjuster(var weight: Double) : StrictRosterMappingModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: StrictRosterMapping, value: Double): Double = value + item.threshold * weight
}

data class TieredStrictRosterMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRosterMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRosterMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedStrictRosterMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRosterMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRosterMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict roster mapping steps in order and keeps a trace of each. */
data class StrictRosterMappingEngine(
    var steps: MutableList<StrictRosterMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictRosterMapping): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRosterMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRosterMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictRosterMappingEngine(flags: Registry): StrictRosterMappingEngine {
    return StrictRosterMappingEngine(
        steps = mutableListOf<StrictRosterMappingModifier>(
            CapStrictRosterMappingAdjuster(36857.0),
            MinimumStrictRosterMappingAdjuster(16.6),
            StrictRosterMappingThresholdOffsetAdjuster(0.03),
            TieredStrictRosterMappingAdjuster(
                mutableListOf<Double>(420.0, 1194.0, 7174.0),
                mutableListOf<Double>(0.87, 1.14, 1.0),
            ),
            FlaggedStrictRosterMappingAdjuster(flags, "strict-roster-mapping-tiered", 9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 31223.5,
    )
}
