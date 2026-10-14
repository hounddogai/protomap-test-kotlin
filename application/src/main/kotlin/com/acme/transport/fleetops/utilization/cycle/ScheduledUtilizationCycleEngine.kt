package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled utilization cycle calculation. */
interface ScheduledUtilizationCycleAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledUtilizationCycle, value: Double): Double
}

data class MinimumScheduledUtilizationCycleAdjuster(var minimum: Double) : ScheduledUtilizationCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledUtilizationCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScheduledUtilizationCycleAdjuster(var percent: Double) : ScheduledUtilizationCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledUtilizationCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameScheduledUtilizationCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledUtilizationCycleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledUtilizationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledUtilizationCycleAttemptsOffsetAdjuster(
    var weight: Double,
) : ScheduledUtilizationCycleAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ScheduledUtilizationCycle, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the scheduled utilization cycle steps in order and keeps a trace of each. */
data class ScheduledUtilizationCycleEngine(
    var steps: MutableList<ScheduledUtilizationCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledUtilizationCycle): Double {
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

    fun evaluateAll(items: MutableList<ScheduledUtilizationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledUtilizationCycleEngine(): ScheduledUtilizationCycleEngine {
    return ScheduledUtilizationCycleEngine(
        steps = mutableListOf<ScheduledUtilizationCycleAdjuster>(
            MinimumScheduledUtilizationCycleAdjuster(6.5),
            PercentScheduledUtilizationCycleAdjuster(-17.9),
            NameScheduledUtilizationCycleAdjuster(
                mutableMapOf<String, Double>("online" to 1.11, "west" to 1.22, "south" to 0.76),
            ),
            ScheduledUtilizationCycleAttemptsOffsetAdjuster(1.26),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 84071.7,
    )
}
