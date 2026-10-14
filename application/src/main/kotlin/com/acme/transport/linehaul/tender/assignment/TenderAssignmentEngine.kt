package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tender assignment calculation. */
interface TenderAssignmentStep {
    fun describe(): String
    fun adjust(item: TenderAssignment, value: Double): Double
}

data class LocaleTenderAssignmentAdjuster(var factors: MutableMap<String, Double>) : TenderAssignmentStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: TenderAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTenderAssignmentAdjuster(var minimum: Double) : TenderAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTenderAssignmentAdjuster(var step: Double) : TenderAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedTenderAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tender assignment steps in order and keeps a trace of each. */
data class TenderAssignmentEngine(
    var steps: MutableList<TenderAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TenderAssignment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<TenderAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTenderAssignmentEngine(flags: Registry): TenderAssignmentEngine {
    return TenderAssignmentEngine(
        steps = mutableListOf<TenderAssignmentStep>(
            LocaleTenderAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 0.8, "online" to 1.07, "retail" to 0.73),
            ),
            MinimumTenderAssignmentAdjuster(3.4),
            RoundingTenderAssignmentAdjuster(1.0),
            FlaggedTenderAssignmentAdjuster(flags, "tender-assignment-primary", -4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 96476.8,
    )
}
