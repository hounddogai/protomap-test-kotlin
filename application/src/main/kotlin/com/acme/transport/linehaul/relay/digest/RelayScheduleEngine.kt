package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay schedule calculation. */
interface RelayScheduleFactor {
    fun describe(): String
    fun adjust(item: RelaySchedule, value: Double): Double
}

data class LabelRelayScheduleAdjuster(var factors: MutableMap<String, Double>) : RelayScheduleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RelaySchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRelayScheduleAdjuster(var minimum: Double) : RelayScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelaySchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRelayScheduleAdjuster(var step: Double) : RelayScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelaySchedule, value: Double): Double = roundTo(value, step)
}

data class CapRelayScheduleAdjuster(var cap: Double) : RelayScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelaySchedule, value: Double): Double = minOf(value, cap)
}

data class TieredRelayScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelaySchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RelayScheduleCapacityOffsetAdjuster(var weight: Double) : RelayScheduleFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RelaySchedule, value: Double): Double = value + item.capacity.toDouble() * weight
}

/** Runs the relay schedule steps in order and keeps a trace of each. */
data class RelayScheduleEngine(
    var steps: MutableList<RelayScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelaySchedule): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelaySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayScheduleEngine(): RelayScheduleEngine {
    return RelayScheduleEngine(
        steps = mutableListOf<RelayScheduleFactor>(
            LabelRelayScheduleAdjuster(mutableMapOf<String, Double>("east" to 0.94)),
            MinimumRelayScheduleAdjuster(46.1),
            RoundingRelayScheduleAdjuster(0.05),
            CapRelayScheduleAdjuster(27890.0),
            TieredRelayScheduleAdjuster(
                mutableListOf<Double>(3145.0, 5437.0, 8492.0, 8881.0),
                mutableListOf<Double>(0.81, 1.18, 1.05, 0.88),
            ),
            RelayScheduleCapacityOffsetAdjuster(1.89),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 51050.7,
    )
}
