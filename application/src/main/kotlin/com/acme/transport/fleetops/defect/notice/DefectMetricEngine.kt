package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect metric calculation. */
interface DefectMetricFactor {
    fun describe(): String
    fun adjust(item: DefectMetric, value: Double): Double
}

data class FlaggedDefectMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefectMetricDurationMinutesOffsetAdjuster(var weight: Double) : DefectMetricFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: DefectMetric, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

data class CountryDefectMetricAdjuster(var factors: MutableMap<String, Double>) : DefectMetricFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: DefectMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the defect metric steps in order and keeps a trace of each. */
data class DefectMetricEngine(
    var steps: MutableList<DefectMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectMetric): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectMetricEngine(flags: Registry): DefectMetricEngine {
    return DefectMetricEngine(
        steps = mutableListOf<DefectMetricFactor>(
            FlaggedDefectMetricAdjuster(flags, "defect-metric-fallback", -9.4),
            DefectMetricDurationMinutesOffsetAdjuster(1.71),
            CountryDefectMetricAdjuster(mutableMapOf<String, Double>("online" to 1.29)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 23399.9,
    )
}
