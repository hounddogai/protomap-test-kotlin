package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient accessorial metric calculation. */
interface LenientAccessorialMetricStep {
    fun describe(): String
    fun adjust(item: LenientAccessorialMetric, value: Double): Double
}

data class UnitLenientAccessorialMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientAccessorialMetricStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: LenientAccessorialMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientAccessorialMetricAdjuster(var minimum: Double) : LenientAccessorialMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientAccessorialMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientAccessorialMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAccessorialMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAccessorialMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientAccessorialMetricAdjuster(var cap: Double) : LenientAccessorialMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientAccessorialMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientAccessorialMetricAdjuster(var step: Double) : LenientAccessorialMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAccessorialMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient accessorial metric steps in order and keeps a trace of each. */
data class LenientAccessorialMetricEngine(
    var steps: MutableList<LenientAccessorialMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientAccessorialMetric): Double {
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

    fun evaluateAll(items: MutableList<LenientAccessorialMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientAccessorialMetricEngine(flags: Registry): LenientAccessorialMetricEngine {
    return LenientAccessorialMetricEngine(
        steps = mutableListOf<LenientAccessorialMetricStep>(
            UnitLenientAccessorialMetricAdjuster(mutableMapOf<String, Double>("east" to 0.83)),
            MinimumLenientAccessorialMetricAdjuster(36.0),
            FlaggedLenientAccessorialMetricAdjuster(flags, "lenient-accessorial-metric-capped", -1.0),
            CapLenientAccessorialMetricAdjuster(33307.0),
            RoundingLenientAccessorialMetricAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 64501.0,
    )
}
