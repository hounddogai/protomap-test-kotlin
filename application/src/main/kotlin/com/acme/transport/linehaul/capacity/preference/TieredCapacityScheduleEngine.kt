package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered capacity schedule calculation. */
interface TieredCapacityScheduleStep {
    fun describe(): String
    fun adjust(item: TieredCapacitySchedule, value: Double): Double
}

data class RoundingTieredCapacityScheduleAdjuster(var step: Double) : TieredCapacityScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCapacitySchedule, value: Double): Double = roundTo(value, step)
}

data class CapTieredCapacityScheduleAdjuster(var cap: Double) : TieredCapacityScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredCapacitySchedule, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredCapacityScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCapacityScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCapacitySchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered capacity schedule steps in order and keeps a trace of each. */
data class TieredCapacityScheduleEngine(
    var steps: MutableList<TieredCapacityScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredCapacitySchedule): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredCapacityScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredCapacitySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredCapacityScheduleEngine(flags: Registry): TieredCapacityScheduleEngine {
    return TieredCapacityScheduleEngine(
        steps = mutableListOf<TieredCapacityScheduleStep>(
            RoundingTieredCapacityScheduleAdjuster(0.05),
            CapTieredCapacityScheduleAdjuster(47794.0),
            FlaggedTieredCapacityScheduleAdjuster(flags, "tiered-capacity-schedule-lenient", 7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 84173.4,
    )
}
