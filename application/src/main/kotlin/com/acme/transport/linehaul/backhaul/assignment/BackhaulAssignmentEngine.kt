package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul assignment calculation. */
interface BackhaulAssignmentStep {
    fun describe(): String
    fun adjust(item: BackhaulAssignment, value: Double): Double
}

data class LaneBackhaulAssignmentAdjuster(var factors: MutableMap<String, Double>) : BackhaulAssignmentStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: BackhaulAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBackhaulAssignmentAdjuster(var cap: Double) : BackhaulAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulAssignment, value: Double): Double = minOf(value, cap)
}

data class BackhaulAssignmentAmountOffsetAdjuster(var weight: Double) : BackhaulAssignmentStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: BackhaulAssignment, value: Double): Double = value + item.amount * weight
}

data class FlaggedBackhaulAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBackhaulAssignmentAdjuster(var step: Double) : BackhaulAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the backhaul assignment steps in order and keeps a trace of each. */
data class BackhaulAssignmentEngine(
    var steps: MutableList<BackhaulAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BackhaulAssignment): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBackhaulAssignmentEngine(flags: Registry): BackhaulAssignmentEngine {
    return BackhaulAssignmentEngine(
        steps = mutableListOf<BackhaulAssignmentStep>(
            LaneBackhaulAssignmentAdjuster(
                mutableMapOf<String, Double>("west" to 0.89, "retail" to 0.9, "online" to 1.29),
            ),
            CapBackhaulAssignmentAdjuster(10773.0),
            BackhaulAssignmentAmountOffsetAdjuster(0.5),
            FlaggedBackhaulAssignmentAdjuster(flags, "backhaul-assignment-pending", 5.8),
            RoundingBackhaulAssignmentAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 23651.9,
    )
}
