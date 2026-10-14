package com.acme.transport.crewing.availability.defaultavailabilitysegment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict availability exception calculation. */
interface StrictAvailabilityExceptionFactor {
    fun describe(): String
    fun adjust(item: StrictAvailabilityException, value: Double): Double
}

data class LabelStrictAvailabilityExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictAvailabilityExceptionFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictAvailabilityException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingStrictAvailabilityExceptionAdjuster(var step: Double) : StrictAvailabilityExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictAvailabilityException, value: Double): Double = roundTo(value, step)
}

data class TieredStrictAvailabilityExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictAvailabilityExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictAvailabilityException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictAvailabilityExceptionAdjuster(var minimum: Double) : StrictAvailabilityExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictAvailabilityException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict availability exception steps in order and keeps a trace of each. */
data class StrictAvailabilityExceptionEngine(
    var steps: MutableList<StrictAvailabilityExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictAvailabilityException): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictAvailabilityException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictAvailabilityExceptionEngine(): StrictAvailabilityExceptionEngine {
    return StrictAvailabilityExceptionEngine(
        steps = mutableListOf<StrictAvailabilityExceptionFactor>(
            LabelStrictAvailabilityExceptionAdjuster(mutableMapOf<String, Double>("retail" to 0.89)),
            RoundingStrictAvailabilityExceptionAdjuster(5.0),
            TieredStrictAvailabilityExceptionAdjuster(
                mutableListOf<Double>(782.0, 1479.0, 2224.0, 7484.0),
                mutableListOf<Double>(1.09, 0.87, 0.82, 0.87),
            ),
            MinimumStrictAvailabilityExceptionAdjuster(16.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 79363.2,
    )
}
