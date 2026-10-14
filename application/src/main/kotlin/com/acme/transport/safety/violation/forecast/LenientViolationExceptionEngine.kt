package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient violation exception calculation. */
interface LenientViolationExceptionStep {
    fun describe(): String
    fun adjust(item: LenientViolationException, value: Double): Double
}

data class TieredLenientViolationExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientViolationExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientViolationException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientViolationExceptionAdjuster(var percent: Double) : LenientViolationExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientViolationException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientViolationExceptionAdjuster(var cap: Double) : LenientViolationExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientViolationException, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientViolationExceptionAdjuster(var step: Double) : LenientViolationExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientViolationException, value: Double): Double = roundTo(value, step)
}

data class TitleLenientViolationExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientViolationExceptionStep {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientViolationException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient violation exception steps in order and keeps a trace of each. */
data class LenientViolationExceptionEngine(
    var steps: MutableList<LenientViolationExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientViolationException): Double {
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

    fun evaluateAll(items: MutableList<LenientViolationException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientViolationExceptionEngine(): LenientViolationExceptionEngine {
    return LenientViolationExceptionEngine(
        steps = mutableListOf<LenientViolationExceptionStep>(
            TieredLenientViolationExceptionAdjuster(
                mutableListOf<Double>(477.0, 2906.0, 3284.0),
                mutableListOf<Double>(0.88, 1.12, 1.07),
            ),
            PercentLenientViolationExceptionAdjuster(6.1),
            CapLenientViolationExceptionAdjuster(45914.0),
            RoundingLenientViolationExceptionAdjuster(0.25),
            TitleLenientViolationExceptionAdjuster(mutableMapOf<String, Double>("east" to 1.23, "west" to 1.2)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 56086.9,
    )
}
