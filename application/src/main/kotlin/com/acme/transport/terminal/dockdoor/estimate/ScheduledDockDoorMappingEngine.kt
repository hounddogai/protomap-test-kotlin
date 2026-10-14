package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled dock door mapping calculation. */
interface ScheduledDockDoorMappingStep {
    fun describe(): String
    fun adjust(item: ScheduledDockDoorMapping, value: Double): Double
}

data class CountryScheduledDockDoorMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDockDoorMappingStep {
    override fun describe(): String = "by country"

    override fun adjust(item: ScheduledDockDoorMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledDockDoorMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDockDoorMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDockDoorMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredScheduledDockDoorMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDockDoorMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDockDoorMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled dock door mapping steps in order and keeps a trace of each. */
data class ScheduledDockDoorMappingEngine(
    var steps: MutableList<ScheduledDockDoorMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledDockDoorMapping): Double {
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

    fun evaluateAll(items: MutableList<ScheduledDockDoorMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledDockDoorMappingEngine(flags: Registry): ScheduledDockDoorMappingEngine {
    return ScheduledDockDoorMappingEngine(
        steps = mutableListOf<ScheduledDockDoorMappingStep>(
            CountryScheduledDockDoorMappingAdjuster(
                mutableMapOf<String, Double>("east" to 0.9, "central" to 1.32),
            ),
            FlaggedScheduledDockDoorMappingAdjuster(flags, "scheduled-dock-door-mapping-scheduled", -2.6),
            TieredScheduledDockDoorMappingAdjuster(
                mutableListOf<Double>(2813.0, 7292.0, 7897.0, 9033.0),
                mutableListOf<Double>(0.82, 1.19, 0.8, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 94089.6,
    )
}
