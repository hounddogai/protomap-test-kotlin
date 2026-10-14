package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient camera event metric calculation. */
interface LenientCameraEventMetricModifier {
    fun describe(): String
    fun adjust(item: LenientCameraEventMetric, value: Double): Double
}

data class RoundingLenientCameraEventMetricAdjuster(var step: Double) : LenientCameraEventMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientCameraEventMetric, value: Double): Double = roundTo(value, step)
}

data class CountryLenientCameraEventMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCameraEventMetricModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientCameraEventMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientCameraEventMetricAdjuster(var cap: Double) : LenientCameraEventMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCameraEventMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient camera event metric steps in order and keeps a trace of each. */
data class LenientCameraEventMetricEngine(
    var steps: MutableList<LenientCameraEventMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientCameraEventMetric): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientCameraEventMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientCameraEventMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientCameraEventMetricEngine(): LenientCameraEventMetricEngine {
    return LenientCameraEventMetricEngine(
        steps = mutableListOf<LenientCameraEventMetricModifier>(
            RoundingLenientCameraEventMetricAdjuster(5.0),
            CountryLenientCameraEventMetricAdjuster(
                mutableMapOf<String, Double>("central" to 1.08, "east" to 1.24, "online" to 1.01),
            ),
            CapLenientCameraEventMetricAdjuster(34873.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 2987.5,
    )
}
