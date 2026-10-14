package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual trailer metric calculation. */
interface ManualTrailerMetricFactor {
    fun describe(): String
    fun adjust(item: ManualTrailerMetric, value: Double): Double
}

data class CapManualTrailerMetricAdjuster(var cap: Double) : ManualTrailerMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualTrailerMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTrailerMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTrailerMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualTrailerMetricAdjuster(var percent: Double) : ManualTrailerMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualTrailerMetricGraceDaysOffsetAdjuster(var weight: Double) : ManualTrailerMetricFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: ManualTrailerMetric, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class RoundingManualTrailerMetricAdjuster(var step: Double) : ManualTrailerMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTrailerMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the manual trailer metric steps in order and keeps a trace of each. */
data class ManualTrailerMetricEngine(
    var steps: MutableList<ManualTrailerMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualTrailerMetric): Double {
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

    fun evaluateAll(items: MutableList<ManualTrailerMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualTrailerMetricEngine(flags: Registry): ManualTrailerMetricEngine {
    return ManualTrailerMetricEngine(
        steps = mutableListOf<ManualTrailerMetricFactor>(
            CapManualTrailerMetricAdjuster(34042.0),
            FlaggedManualTrailerMetricAdjuster(flags, "manual-trailer-metric-lenient", 2.2),
            PercentManualTrailerMetricAdjuster(12.1),
            ManualTrailerMetricGraceDaysOffsetAdjuster(1.77),
            RoundingManualTrailerMetricAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 31786.8,
    )
}
