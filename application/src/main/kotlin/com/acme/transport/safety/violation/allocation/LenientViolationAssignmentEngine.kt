package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient violation assignment calculation. */
interface LenientViolationAssignmentModifier {
    fun describe(): String
    fun adjust(item: LenientViolationAssignment, value: Double): Double
}

data class RoundingLenientViolationAssignmentAdjuster(var step: Double) : LenientViolationAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientViolationAssignment, value: Double): Double = roundTo(value, step)
}

data class LenientViolationAssignmentDiscountOffsetAdjuster(
    var weight: Double,
) : LenientViolationAssignmentModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: LenientViolationAssignment, value: Double): Double = value + item.discount * weight
}

data class MinimumLenientViolationAssignmentAdjuster(var minimum: Double) : LenientViolationAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientViolationAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientViolationAssignmentAdjuster(var cap: Double) : LenientViolationAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientViolationAssignment, value: Double): Double = minOf(value, cap)
}

data class LabelLenientViolationAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientViolationAssignmentModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientViolationAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientViolationAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientViolationAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientViolationAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient violation assignment steps in order and keeps a trace of each. */
data class LenientViolationAssignmentEngine(
    var steps: MutableList<LenientViolationAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientViolationAssignment): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientViolationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientViolationAssignmentEngine(): LenientViolationAssignmentEngine {
    return LenientViolationAssignmentEngine(
        steps = mutableListOf<LenientViolationAssignmentModifier>(
            RoundingLenientViolationAssignmentAdjuster(0.1),
            LenientViolationAssignmentDiscountOffsetAdjuster(0.54),
            MinimumLenientViolationAssignmentAdjuster(10.0),
            CapLenientViolationAssignmentAdjuster(34700.0),
            LabelLenientViolationAssignmentAdjuster(mutableMapOf<String, Double>("central" to 0.86)),
            TieredLenientViolationAssignmentAdjuster(
                mutableListOf<Double>(315.0, 1741.0, 5706.0, 7229.0),
                mutableListOf<Double>(0.88, 1.09, 0.98, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 47519.3,
    )
}
