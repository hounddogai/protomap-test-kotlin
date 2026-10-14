package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire assignment calculation. */
interface TireAssignmentFactor {
    fun describe(): String
    fun adjust(item: TireAssignment, value: Double): Double
}

data class TireAssignmentDurationMinutesOffsetAdjuster(var weight: Double) : TireAssignmentFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: TireAssignment, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapTireAssignmentAdjuster(var cap: Double) : TireAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireAssignment, value: Double): Double = minOf(value, cap)
}

data class RegionTireAssignmentAdjuster(var factors: MutableMap<String, Double>) : TireAssignmentFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: TireAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTireAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTireAssignmentAdjuster(var minimum: Double) : TireAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTireAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tire assignment steps in order and keeps a trace of each. */
data class TireAssignmentEngine(
    var steps: MutableList<TireAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TireAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTireAssignmentEngine(flags: Registry): TireAssignmentEngine {
    return TireAssignmentEngine(
        steps = mutableListOf<TireAssignmentFactor>(
            TireAssignmentDurationMinutesOffsetAdjuster(1.9),
            CapTireAssignmentAdjuster(5080.0),
            RegionTireAssignmentAdjuster(mutableMapOf<String, Double>("west" to 1.11, "central" to 0.86)),
            TieredTireAssignmentAdjuster(
                mutableListOf<Double>(2338.0, 3070.0, 3231.0, 6051.0),
                mutableListOf<Double>(0.91, 1.03, 1.15, 1.06),
            ),
            MinimumTireAssignmentAdjuster(3.2),
            FlaggedTireAssignmentAdjuster(flags, "tire-assignment-seasonal", -0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 98447.1,
    )
}
