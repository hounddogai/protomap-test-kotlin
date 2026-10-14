package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled declaration cycle calculation. */
interface ScheduledDeclarationCycleAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledDeclarationCycle, value: Double): Double
}

data class TieredScheduledDeclarationCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDeclarationCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDeclarationCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledDeclarationCycleAdjuster(var cap: Double) : ScheduledDeclarationCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDeclarationCycle, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledDeclarationCycleAdjuster(var minimum: Double) : ScheduledDeclarationCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDeclarationCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledDeclarationCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDeclarationCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDeclarationCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledDeclarationCycleDistanceKmOffsetAdjuster(
    var weight: Double,
) : ScheduledDeclarationCycleAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ScheduledDeclarationCycle, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingScheduledDeclarationCycleAdjuster(var step: Double) : ScheduledDeclarationCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDeclarationCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled declaration cycle steps in order and keeps a trace of each. */
data class ScheduledDeclarationCycleEngine(
    var steps: MutableList<ScheduledDeclarationCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledDeclarationCycle): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDeclarationCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDeclarationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledDeclarationCycleEngine(flags: Registry): ScheduledDeclarationCycleEngine {
    return ScheduledDeclarationCycleEngine(
        steps = mutableListOf<ScheduledDeclarationCycleAdjuster>(
            TieredScheduledDeclarationCycleAdjuster(
                mutableListOf<Double>(2534.0, 5510.0),
                mutableListOf<Double>(0.95, 1.1),
            ),
            CapScheduledDeclarationCycleAdjuster(28318.0),
            MinimumScheduledDeclarationCycleAdjuster(49.5),
            FlaggedScheduledDeclarationCycleAdjuster(flags, "scheduled-declaration-cycle-weighted", 0.4),
            ScheduledDeclarationCycleDistanceKmOffsetAdjuster(1.97),
            RoundingScheduledDeclarationCycleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 86682.5,
    )
}
