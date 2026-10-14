package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled defect entry calculation. */
interface ScheduledDefectEntryModifier {
    fun describe(): String
    fun adjust(item: ScheduledDefectEntry, value: Double): Double
}

data class MinimumScheduledDefectEntryAdjuster(var minimum: Double) : ScheduledDefectEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDefectEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScheduledDefectEntryAdjuster(var percent: Double) : ScheduledDefectEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDefectEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionScheduledDefectEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDefectEntryModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledDefectEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledDefectEntryAdjuster(var cap: Double) : ScheduledDefectEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDefectEntry, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledDefectEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDefectEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDefectEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled defect entry steps in order and keeps a trace of each. */
data class ScheduledDefectEntryEngine(
    var steps: MutableList<ScheduledDefectEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledDefectEntry): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledDefectEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledDefectEntryEngine(): ScheduledDefectEntryEngine {
    return ScheduledDefectEntryEngine(
        steps = mutableListOf<ScheduledDefectEntryModifier>(
            MinimumScheduledDefectEntryAdjuster(30.4),
            PercentScheduledDefectEntryAdjuster(-2.1),
            RegionScheduledDefectEntryAdjuster(mutableMapOf<String, Double>("retail" to 1.04)),
            CapScheduledDefectEntryAdjuster(33321.0),
            TieredScheduledDefectEntryAdjuster(
                mutableListOf<Double>(3876.0, 5415.0),
                mutableListOf<Double>(1.04, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 68817.2,
    )
}
