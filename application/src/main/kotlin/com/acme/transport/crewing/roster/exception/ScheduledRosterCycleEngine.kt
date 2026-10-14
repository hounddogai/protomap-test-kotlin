package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled roster cycle calculation. */
interface ScheduledRosterCycleStep {
    fun describe(): String
    fun adjust(item: ScheduledRosterCycle, value: Double): Double
}

data class RoundingScheduledRosterCycleAdjuster(var step: Double) : ScheduledRosterCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRosterCycle, value: Double): Double = roundTo(value, step)
}

data class MinimumScheduledRosterCycleAdjuster(var minimum: Double) : ScheduledRosterCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRosterCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledRosterCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRosterCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRosterCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledRosterCycleSequenceOffsetAdjuster(var weight: Double) : ScheduledRosterCycleStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: ScheduledRosterCycle, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredScheduledRosterCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRosterCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRosterCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledRosterCycleAdjuster(var percent: Double) : ScheduledRosterCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRosterCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled roster cycle steps in order and keeps a trace of each. */
data class ScheduledRosterCycleEngine(
    var steps: MutableList<ScheduledRosterCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRosterCycle): Double {
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

    fun evaluateAll(items: MutableList<ScheduledRosterCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRosterCycleEngine(flags: Registry): ScheduledRosterCycleEngine {
    return ScheduledRosterCycleEngine(
        steps = mutableListOf<ScheduledRosterCycleStep>(
            RoundingScheduledRosterCycleAdjuster(0.25),
            MinimumScheduledRosterCycleAdjuster(3.3),
            FlaggedScheduledRosterCycleAdjuster(flags, "scheduled-roster-cycle-batched", 1.3),
            ScheduledRosterCycleSequenceOffsetAdjuster(1.88),
            TieredScheduledRosterCycleAdjuster(
                mutableListOf<Double>(624.0, 4641.0, 7212.0, 7530.0),
                mutableListOf<Double>(1.17, 1.2, 0.89, 1.16),
            ),
            PercentScheduledRosterCycleAdjuster(-3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 45194.2,
    )
}
