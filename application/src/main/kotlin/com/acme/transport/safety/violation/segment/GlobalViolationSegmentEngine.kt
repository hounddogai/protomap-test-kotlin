package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global violation segment calculation. */
interface GlobalViolationSegmentFactor {
    fun describe(): String
    fun adjust(item: GlobalViolationSegment, value: Double): Double
}

data class RoundingGlobalViolationSegmentAdjuster(var step: Double) : GlobalViolationSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalViolationSegment, value: Double): Double = roundTo(value, step)
}

data class CapGlobalViolationSegmentAdjuster(var cap: Double) : GlobalViolationSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalViolationSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalViolationSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalViolationSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalViolationSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global violation segment steps in order and keeps a trace of each. */
data class GlobalViolationSegmentEngine(
    var steps: MutableList<GlobalViolationSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalViolationSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalViolationSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalViolationSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalViolationSegmentEngine(flags: Registry): GlobalViolationSegmentEngine {
    return GlobalViolationSegmentEngine(
        steps = mutableListOf<GlobalViolationSegmentFactor>(
            RoundingGlobalViolationSegmentAdjuster(0.1),
            CapGlobalViolationSegmentAdjuster(18851.0),
            FlaggedGlobalViolationSegmentAdjuster(flags, "global-violation-segment-manual", -2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 45960.1,
    )
}
