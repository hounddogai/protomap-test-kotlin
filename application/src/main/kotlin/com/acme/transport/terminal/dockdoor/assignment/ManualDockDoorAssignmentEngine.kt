package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual dock door assignment calculation. */
interface ManualDockDoorAssignmentModifier {
    fun describe(): String
    fun adjust(item: ManualDockDoorAssignment, value: Double): Double
}

data class TieredManualDockDoorAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDockDoorAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDockDoorAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingManualDockDoorAssignmentAdjuster(var step: Double) : ManualDockDoorAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDockDoorAssignment, value: Double): Double = roundTo(value, step)
}

data class PercentManualDockDoorAssignmentAdjuster(var percent: Double) : ManualDockDoorAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDockDoorAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleManualDockDoorAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDockDoorAssignmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualDockDoorAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualDockDoorAssignmentAdjuster(var minimum: Double) : ManualDockDoorAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDockDoorAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualDockDoorAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDockDoorAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDockDoorAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual dock door assignment steps in order and keeps a trace of each. */
data class ManualDockDoorAssignmentEngine(
    var steps: MutableList<ManualDockDoorAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualDockDoorAssignment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDockDoorAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualDockDoorAssignmentEngine(flags: Registry): ManualDockDoorAssignmentEngine {
    return ManualDockDoorAssignmentEngine(
        steps = mutableListOf<ManualDockDoorAssignmentModifier>(
            TieredManualDockDoorAssignmentAdjuster(
                mutableListOf<Double>(4438.0, 7872.0),
                mutableListOf<Double>(0.88, 1.1),
            ),
            RoundingManualDockDoorAssignmentAdjuster(0.5),
            PercentManualDockDoorAssignmentAdjuster(21.5),
            TitleManualDockDoorAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 0.83, "east" to 1.12),
            ),
            MinimumManualDockDoorAssignmentAdjuster(46.2),
            FlaggedManualDockDoorAssignmentAdjuster(flags, "manual-dock-door-assignment-seasonal", -3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 68709.0,
    )
}
