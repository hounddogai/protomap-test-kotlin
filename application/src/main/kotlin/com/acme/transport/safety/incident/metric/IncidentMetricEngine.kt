package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the incident metric calculation. */
interface IncidentMetricStep {
    fun describe(): String
    fun adjust(item: IncidentMetric, value: Double): Double
}

data class IncidentMetricSequenceOffsetAdjuster(var weight: Double) : IncidentMetricStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: IncidentMetric, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class MinimumIncidentMetricAdjuster(var minimum: Double) : IncidentMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapIncidentMetricAdjuster(var cap: Double) : IncidentMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedIncidentMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredIncidentMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the incident metric steps in order and keeps a trace of each. */
data class IncidentMetricEngine(
    var steps: MutableList<IncidentMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentMetric): Double {
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

    fun evaluateAll(items: MutableList<IncidentMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentMetricEngine(flags: Registry): IncidentMetricEngine {
    return IncidentMetricEngine(
        steps = mutableListOf<IncidentMetricStep>(
            IncidentMetricSequenceOffsetAdjuster(0.77),
            MinimumIncidentMetricAdjuster(33.9),
            CapIncidentMetricAdjuster(16721.0),
            FlaggedIncidentMetricAdjuster(flags, "incident-metric-strict", -8.7),
            TieredIncidentMetricAdjuster(
                mutableListOf<Double>(4838.0, 9531.0),
                mutableListOf<Double>(1.2, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 51960.4,
    )
}
