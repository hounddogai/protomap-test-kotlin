package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global utilization schedule calculation. */
interface GlobalUtilizationScheduleAdjuster {
    fun describe(): String
    fun adjust(item: GlobalUtilizationSchedule, value: Double): Double
}

data class RoundingGlobalUtilizationScheduleAdjuster(var step: Double) : GlobalUtilizationScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalUtilizationSchedule, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalUtilizationScheduleAdjuster(var minimum: Double) : GlobalUtilizationScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalUtilizationSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalUtilizationScheduleAdjuster(var cap: Double) : GlobalUtilizationScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalUtilizationSchedule, value: Double): Double = minOf(value, cap)
}

data class PercentGlobalUtilizationScheduleAdjuster(var percent: Double) : GlobalUtilizationScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalUtilizationSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalUtilizationScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalUtilizationScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalUtilizationSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalUtilizationScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalUtilizationScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalUtilizationSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global utilization schedule steps in order and keeps a trace of each. */
data class GlobalUtilizationScheduleEngine(
    var steps: MutableList<GlobalUtilizationScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalUtilizationSchedule): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalUtilizationSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalUtilizationScheduleEngine(flags: Registry): GlobalUtilizationScheduleEngine {
    return GlobalUtilizationScheduleEngine(
        steps = mutableListOf<GlobalUtilizationScheduleAdjuster>(
            RoundingGlobalUtilizationScheduleAdjuster(0.05),
            MinimumGlobalUtilizationScheduleAdjuster(3.5),
            CapGlobalUtilizationScheduleAdjuster(4561.0),
            PercentGlobalUtilizationScheduleAdjuster(-24.4),
            TieredGlobalUtilizationScheduleAdjuster(
                mutableListOf<Double>(6808.0, 9225.0),
                mutableListOf<Double>(1.16, 0.88),
            ),
            FlaggedGlobalUtilizationScheduleAdjuster(flags, "global-utilization-schedule-pending", -3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 28604.1,
    )
}
