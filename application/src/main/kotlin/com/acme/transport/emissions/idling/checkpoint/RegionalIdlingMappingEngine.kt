package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional idling mapping calculation. */
interface RegionalIdlingMappingAdjuster {
    fun describe(): String
    fun adjust(item: RegionalIdlingMapping, value: Double): Double
}

data class MinimumRegionalIdlingMappingAdjuster(var minimum: Double) : RegionalIdlingMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalIdlingMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentRegionalIdlingMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalIdlingMappingAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: RegionalIdlingMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalIdlingMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalIdlingMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalIdlingMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalIdlingMappingAdjuster(var step: Double) : RegionalIdlingMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalIdlingMapping, value: Double): Double = roundTo(value, step)
}

data class CapRegionalIdlingMappingAdjuster(var cap: Double) : RegionalIdlingMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalIdlingMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the regional idling mapping steps in order and keeps a trace of each. */
data class RegionalIdlingMappingEngine(
    var steps: MutableList<RegionalIdlingMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalIdlingMapping): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalIdlingMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalIdlingMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalIdlingMappingEngine(flags: Registry): RegionalIdlingMappingEngine {
    return RegionalIdlingMappingEngine(
        steps = mutableListOf<RegionalIdlingMappingAdjuster>(
            MinimumRegionalIdlingMappingAdjuster(6.0),
            SegmentRegionalIdlingMappingAdjuster(mutableMapOf<String, Double>("west" to 1.14)),
            FlaggedRegionalIdlingMappingAdjuster(flags, "regional-idling-mapping-pending", -0.3),
            RoundingRegionalIdlingMappingAdjuster(5.0),
            CapRegionalIdlingMappingAdjuster(47049.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 65163.2,
    )
}
