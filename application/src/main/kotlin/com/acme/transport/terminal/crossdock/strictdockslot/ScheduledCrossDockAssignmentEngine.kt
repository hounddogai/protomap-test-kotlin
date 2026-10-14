package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled cross dock assignment calculation. */
interface ScheduledCrossDockAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledCrossDockAssignment, value: Double): Double
}

data class RoundingScheduledCrossDockAssignmentAdjuster(
    var step: Double,
) : ScheduledCrossDockAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledCrossDockAssignment, value: Double): Double = roundTo(value, step)
}

data class RegionScheduledCrossDockAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledCrossDockAssignmentAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledCrossDockAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentScheduledCrossDockAssignmentAdjuster(
    var percent: Double,
) : ScheduledCrossDockAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledCrossDockAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledCrossDockAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledCrossDockAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledCrossDockAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledCrossDockAssignmentAdjuster(
    var minimum: Double,
) : ScheduledCrossDockAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledCrossDockAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledCrossDockAssignmentVersionOffsetAdjuster(
    var weight: Double,
) : ScheduledCrossDockAssignmentAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ScheduledCrossDockAssignment, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

/** Runs the scheduled cross dock assignment steps in order and keeps a trace of each. */
data class ScheduledCrossDockAssignmentEngine(
    var steps: MutableList<ScheduledCrossDockAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledCrossDockAssignment): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledCrossDockAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledCrossDockAssignmentEngine(flags: Registry): ScheduledCrossDockAssignmentEngine {
    return ScheduledCrossDockAssignmentEngine(
        steps = mutableListOf<ScheduledCrossDockAssignmentAdjuster>(
            RoundingScheduledCrossDockAssignmentAdjuster(1.0),
            RegionScheduledCrossDockAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.11, "south" to 0.89, "east" to 0.75),
            ),
            PercentScheduledCrossDockAssignmentAdjuster(-18.9),
            FlaggedScheduledCrossDockAssignmentAdjuster(flags, "scheduled-cross-dock-assignment-strict", 0.3),
            MinimumScheduledCrossDockAssignmentAdjuster(30.7),
            ScheduledCrossDockAssignmentVersionOffsetAdjuster(1.12),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 61382.5,
    )
}
