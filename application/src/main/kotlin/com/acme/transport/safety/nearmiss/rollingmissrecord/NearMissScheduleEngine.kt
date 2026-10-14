package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss schedule calculation. */
interface NearMissScheduleAdjuster {
    fun describe(): String
    fun adjust(item: NearMissSchedule, value: Double): Double
}

data class FlaggedNearMissScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapNearMissScheduleAdjuster(var cap: Double) : NearMissScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissSchedule, value: Double): Double = minOf(value, cap)
}

data class TieredNearMissScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingNearMissScheduleAdjuster(var step: Double) : NearMissScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissSchedule, value: Double): Double = roundTo(value, step)
}

data class MinimumNearMissScheduleAdjuster(var minimum: Double) : NearMissScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NearMissScheduleWeightOffsetAdjuster(var weight: Double) : NearMissScheduleAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: NearMissSchedule, value: Double): Double = value + item.weight * weight
}

/** Runs the near miss schedule steps in order and keeps a trace of each. */
data class NearMissScheduleEngine(
    var steps: MutableList<NearMissScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NearMissSchedule): Double {
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

    fun evaluateAll(items: MutableList<NearMissSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNearMissScheduleEngine(flags: Registry): NearMissScheduleEngine {
    return NearMissScheduleEngine(
        steps = mutableListOf<NearMissScheduleAdjuster>(
            FlaggedNearMissScheduleAdjuster(flags, "near-miss-schedule-primary", -2.9),
            CapNearMissScheduleAdjuster(2589.0),
            TieredNearMissScheduleAdjuster(
                mutableListOf<Double>(5871.0, 7359.0, 7578.0, 8285.0),
                mutableListOf<Double>(1.07, 1.01, 0.86, 0.89),
            ),
            RoundingNearMissScheduleAdjuster(0.1),
            MinimumNearMissScheduleAdjuster(35.2),
            NearMissScheduleWeightOffsetAdjuster(1.87),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 27480.6,
    )
}
