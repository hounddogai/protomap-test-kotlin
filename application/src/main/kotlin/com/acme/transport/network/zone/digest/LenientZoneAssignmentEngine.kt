package com.acme.transport.network.zone.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient zone assignment calculation. */
interface LenientZoneAssignmentFactor {
    fun describe(): String
    fun adjust(item: LenientZoneAssignment, value: Double): Double
}

data class MinimumLenientZoneAssignmentAdjuster(var minimum: Double) : LenientZoneAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientZoneAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientZoneAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientZoneAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientZoneAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientZoneAssignmentGraceDaysOffsetAdjuster(var weight: Double) : LenientZoneAssignmentFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: LenientZoneAssignment, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class RoundingLenientZoneAssignmentAdjuster(var step: Double) : LenientZoneAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientZoneAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient zone assignment steps in order and keeps a trace of each. */
data class LenientZoneAssignmentEngine(
    var steps: MutableList<LenientZoneAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientZoneAssignment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientZoneAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientZoneAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientZoneAssignmentEngine(flags: Registry): LenientZoneAssignmentEngine {
    return LenientZoneAssignmentEngine(
        steps = mutableListOf<LenientZoneAssignmentFactor>(
            MinimumLenientZoneAssignmentAdjuster(46.2),
            FlaggedLenientZoneAssignmentAdjuster(flags, "lenient-zone-assignment-adaptive", -0.2),
            LenientZoneAssignmentGraceDaysOffsetAdjuster(1.26),
            RoundingLenientZoneAssignmentAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 66470.2,
    )
}
