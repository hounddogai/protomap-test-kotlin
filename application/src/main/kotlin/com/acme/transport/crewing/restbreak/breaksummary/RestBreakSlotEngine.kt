package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break slot calculation. */
interface RestBreakSlotStep {
    fun describe(): String
    fun adjust(item: RestBreakSlot, value: Double): Double
}

data class RoundingRestBreakSlotAdjuster(var step: Double) : RestBreakSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakSlot, value: Double): Double = roundTo(value, step)
}

data class MinimumRestBreakSlotAdjuster(var minimum: Double) : RestBreakSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRestBreakSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rest break slot steps in order and keeps a trace of each. */
data class RestBreakSlotEngine(
    var steps: MutableList<RestBreakSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakSlot): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakSlotEngine(flags: Registry): RestBreakSlotEngine {
    return RestBreakSlotEngine(
        steps = mutableListOf<RestBreakSlotStep>(
            RoundingRestBreakSlotAdjuster(0.1),
            MinimumRestBreakSlotAdjuster(17.3),
            FlaggedRestBreakSlotAdjuster(flags, "rest-break-slot-lenient", 6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 60498.6,
    )
}
