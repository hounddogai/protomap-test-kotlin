package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the drop assignment calculation. */
interface DropAssignmentStep {
    fun describe(): String
    fun adjust(item: DropAssignment, value: Double): Double
}

data class TieredDropAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryDropAssignmentAdjuster(var factors: MutableMap<String, Double>) : DropAssignmentStep {
    override fun describe(): String = "by category"

    override fun adjust(item: DropAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDropAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDropAssignmentAdjuster(var step: Double) : DropAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the drop assignment steps in order and keeps a trace of each. */
data class DropAssignmentEngine(
    var steps: MutableList<DropAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DropAssignment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<DropAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDropAssignmentEngine(flags: Registry): DropAssignmentEngine {
    return DropAssignmentEngine(
        steps = mutableListOf<DropAssignmentStep>(
            TieredDropAssignmentAdjuster(
                mutableListOf<Double>(2217.0, 4810.0, 9478.0),
                mutableListOf<Double>(1.04, 0.91, 0.84),
            ),
            CategoryDropAssignmentAdjuster(mutableMapOf<String, Double>("south" to 0.71)),
            FlaggedDropAssignmentAdjuster(flags, "drop-assignment-global", 2.3),
            RoundingDropAssignmentAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 42830.1,
    )
}
