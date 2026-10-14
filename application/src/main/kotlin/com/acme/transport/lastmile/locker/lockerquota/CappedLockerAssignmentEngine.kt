package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped locker assignment calculation. */
interface CappedLockerAssignmentModifier {
    fun describe(): String
    fun adjust(item: CappedLockerAssignment, value: Double): Double
}

data class MinimumCappedLockerAssignmentAdjuster(var minimum: Double) : CappedLockerAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedLockerAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedLockerAssignmentAdjuster(var step: Double) : CappedLockerAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedLockerAssignment, value: Double): Double = roundTo(value, step)
}

data class LaneCappedLockerAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedLockerAssignmentModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: CappedLockerAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped locker assignment steps in order and keeps a trace of each. */
data class CappedLockerAssignmentEngine(
    var steps: MutableList<CappedLockerAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedLockerAssignment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedLockerAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedLockerAssignmentEngine(): CappedLockerAssignmentEngine {
    return CappedLockerAssignmentEngine(
        steps = mutableListOf<CappedLockerAssignmentModifier>(
            MinimumCappedLockerAssignmentAdjuster(37.0),
            RoundingCappedLockerAssignmentAdjuster(1.0),
            LaneCappedLockerAssignmentAdjuster(mutableMapOf<String, Double>("east" to 1.06, "west" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 60711.5,
    )
}
