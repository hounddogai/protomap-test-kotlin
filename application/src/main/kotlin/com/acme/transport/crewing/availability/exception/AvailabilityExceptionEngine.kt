package com.acme.transport.crewing.availability.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the availability exception calculation. */
interface AvailabilityExceptionModifier {
    fun describe(): String
    fun adjust(item: AvailabilityException, value: Double): Double
}

data class RoundingAvailabilityExceptionAdjuster(var step: Double) : AvailabilityExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityException, value: Double): Double = roundTo(value, step)
}

data class RegionAvailabilityExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityExceptionModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: AvailabilityException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAvailabilityExceptionAdjuster(var cap: Double) : AvailabilityExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityException, value: Double): Double = minOf(value, cap)
}

data class MinimumAvailabilityExceptionAdjuster(var minimum: Double) : AvailabilityExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the availability exception steps in order and keeps a trace of each. */
data class AvailabilityExceptionEngine(
    var steps: MutableList<AvailabilityExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilityException): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilityExceptionEngine(): AvailabilityExceptionEngine {
    return AvailabilityExceptionEngine(
        steps = mutableListOf<AvailabilityExceptionModifier>(
            RoundingAvailabilityExceptionAdjuster(0.25),
            RegionAvailabilityExceptionAdjuster(mutableMapOf<String, Double>("south" to 1.24)),
            CapAvailabilityExceptionAdjuster(27871.0),
            MinimumAvailabilityExceptionAdjuster(19.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 75900.1,
    )
}
