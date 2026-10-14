package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled availability cycle calculation. */
interface ScheduledAvailabilityCycleFactor {
    fun describe(): String
    fun adjust(item: ScheduledAvailabilityCycle, value: Double): Double
}

data class FlaggedScheduledAvailabilityCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledAvailabilityCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledAvailabilityCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledAvailabilityCycleAdjuster(var percent: Double) : ScheduledAvailabilityCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledAvailabilityCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledAvailabilityCycleScoreOffsetAdjuster(
    var weight: Double,
) : ScheduledAvailabilityCycleFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: ScheduledAvailabilityCycle, value: Double): Double = value + item.score * weight
}

data class MinimumScheduledAvailabilityCycleAdjuster(var minimum: Double) : ScheduledAvailabilityCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledAvailabilityCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledAvailabilityCycleAdjuster(var cap: Double) : ScheduledAvailabilityCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledAvailabilityCycle, value: Double): Double = minOf(value, cap)
}

data class RoundingScheduledAvailabilityCycleAdjuster(var step: Double) : ScheduledAvailabilityCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledAvailabilityCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled availability cycle steps in order and keeps a trace of each. */
data class ScheduledAvailabilityCycleEngine(
    var steps: MutableList<ScheduledAvailabilityCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledAvailabilityCycle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledAvailabilityCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledAvailabilityCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledAvailabilityCycleEngine(flags: Registry): ScheduledAvailabilityCycleEngine {
    return ScheduledAvailabilityCycleEngine(
        steps = mutableListOf<ScheduledAvailabilityCycleFactor>(
            FlaggedScheduledAvailabilityCycleAdjuster(flags, "scheduled-availability-cycle-global", -3.7),
            PercentScheduledAvailabilityCycleAdjuster(-23.7),
            ScheduledAvailabilityCycleScoreOffsetAdjuster(0.77),
            MinimumScheduledAvailabilityCycleAdjuster(7.9),
            CapScheduledAvailabilityCycleAdjuster(46208.0),
            RoundingScheduledAvailabilityCycleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 74897.9,
    )
}
