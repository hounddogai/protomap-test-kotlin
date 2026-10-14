package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global fuel burn schedule calculation. */
interface GlobalFuelBurnScheduleFactor {
    fun describe(): String
    fun adjust(item: GlobalFuelBurnSchedule, value: Double): Double
}

data class TieredGlobalFuelBurnScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalFuelBurnScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalFuelBurnSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class GlobalFuelBurnScheduleUpdatedAtOffsetAdjuster(var weight: Double) : GlobalFuelBurnScheduleFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: GlobalFuelBurnSchedule, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class CountryGlobalFuelBurnScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalFuelBurnScheduleFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: GlobalFuelBurnSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalFuelBurnScheduleAdjuster(var minimum: Double) : GlobalFuelBurnScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalFuelBurnSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalFuelBurnScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalFuelBurnScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalFuelBurnSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalFuelBurnScheduleAdjuster(var step: Double) : GlobalFuelBurnScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalFuelBurnSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the global fuel burn schedule steps in order and keeps a trace of each. */
data class GlobalFuelBurnScheduleEngine(
    var steps: MutableList<GlobalFuelBurnScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalFuelBurnSchedule): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalFuelBurnScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalFuelBurnSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalFuelBurnScheduleEngine(flags: Registry): GlobalFuelBurnScheduleEngine {
    return GlobalFuelBurnScheduleEngine(
        steps = mutableListOf<GlobalFuelBurnScheduleFactor>(
            TieredGlobalFuelBurnScheduleAdjuster(
                mutableListOf<Double>(1686.0, 3716.0, 8143.0),
                mutableListOf<Double>(0.94, 1.01, 0.9),
            ),
            GlobalFuelBurnScheduleUpdatedAtOffsetAdjuster(1.22),
            CountryGlobalFuelBurnScheduleAdjuster(
                mutableMapOf<String, Double>("online" to 1.15, "retail" to 0.92),
            ),
            MinimumGlobalFuelBurnScheduleAdjuster(10.0),
            FlaggedGlobalFuelBurnScheduleAdjuster(flags, "global-fuel-burn-schedule-strict", -6.2),
            RoundingGlobalFuelBurnScheduleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 84017.1,
    )
}
