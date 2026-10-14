package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global incident metric calculation. */
interface GlobalIncidentMetricModifier {
    fun describe(): String
    fun adjust(item: GlobalIncidentMetric, value: Double): Double
}

data class RoundingGlobalIncidentMetricAdjuster(var step: Double) : GlobalIncidentMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalIncidentMetric, value: Double): Double = roundTo(value, step)
}

data class TitleGlobalIncidentMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalIncidentMetricModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalIncidentMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredGlobalIncidentMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalIncidentMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalIncidentMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalIncidentMetricAdjuster(var percent: Double) : GlobalIncidentMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalIncidentMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapGlobalIncidentMetricAdjuster(var cap: Double) : GlobalIncidentMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalIncidentMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the global incident metric steps in order and keeps a trace of each. */
data class GlobalIncidentMetricEngine(
    var steps: MutableList<GlobalIncidentMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalIncidentMetric): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalIncidentMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalIncidentMetricEngine(): GlobalIncidentMetricEngine {
    return GlobalIncidentMetricEngine(
        steps = mutableListOf<GlobalIncidentMetricModifier>(
            RoundingGlobalIncidentMetricAdjuster(0.25),
            TitleGlobalIncidentMetricAdjuster(mutableMapOf<String, Double>("online" to 1.26, "east" to 0.7)),
            TieredGlobalIncidentMetricAdjuster(
                mutableListOf<Double>(1799.0, 6822.0, 8057.0),
                mutableListOf<Double>(1.15, 1.15, 0.86),
            ),
            PercentGlobalIncidentMetricAdjuster(-6.1),
            CapGlobalIncidentMetricAdjuster(11126.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 20304.9,
    )
}
