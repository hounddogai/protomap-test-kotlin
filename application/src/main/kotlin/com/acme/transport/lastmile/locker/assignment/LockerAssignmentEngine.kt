package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the locker assignment calculation. */
interface LockerAssignmentModifier {
    fun describe(): String
    fun adjust(item: LockerAssignment, value: Double): Double
}

data class MinimumLockerAssignmentAdjuster(var minimum: Double) : LockerAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLockerAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LockerAssignmentAttemptsOffsetAdjuster(var weight: Double) : LockerAssignmentModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: LockerAssignment, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class PercentLockerAssignmentAdjuster(var percent: Double) : LockerAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLockerAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLockerAssignmentAdjuster(var cap: Double) : LockerAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the locker assignment steps in order and keeps a trace of each. */
data class LockerAssignmentEngine(
    var steps: MutableList<LockerAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LockerAssignment): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LockerAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLockerAssignmentEngine(flags: Registry): LockerAssignmentEngine {
    return LockerAssignmentEngine(
        steps = mutableListOf<LockerAssignmentModifier>(
            MinimumLockerAssignmentAdjuster(38.2),
            FlaggedLockerAssignmentAdjuster(flags, "locker-assignment-default", -4.1),
            LockerAssignmentAttemptsOffsetAdjuster(0.61),
            PercentLockerAssignmentAdjuster(-2.0),
            TieredLockerAssignmentAdjuster(
                mutableListOf<Double>(2009.0, 3533.0, 4624.0, 4987.0),
                mutableListOf<Double>(0.9, 0.99, 1.16, 0.89),
            ),
            CapLockerAssignmentAdjuster(7053.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 74462.5,
    )
}
