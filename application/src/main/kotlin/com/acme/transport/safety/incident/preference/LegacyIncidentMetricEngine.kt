package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy incident metric calculation. */
interface LegacyIncidentMetricStep {
    fun describe(): String
    fun adjust(item: LegacyIncidentMetric, value: Double): Double
}

data class RoundingLegacyIncidentMetricAdjuster(var step: Double) : LegacyIncidentMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyIncidentMetric, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyIncidentMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyIncidentMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyIncidentMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyIncidentMetricAdjuster(var percent: Double) : LegacyIncidentMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyIncidentMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy incident metric steps in order and keeps a trace of each. */
data class LegacyIncidentMetricEngine(
    var steps: MutableList<LegacyIncidentMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyIncidentMetric): Double {
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

    fun evaluateAll(items: MutableList<LegacyIncidentMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyIncidentMetricEngine(flags: Registry): LegacyIncidentMetricEngine {
    return LegacyIncidentMetricEngine(
        steps = mutableListOf<LegacyIncidentMetricStep>(
            RoundingLegacyIncidentMetricAdjuster(1.0),
            FlaggedLegacyIncidentMetricAdjuster(flags, "legacy-incident-metric-tiered", 0.8),
            PercentLegacyIncidentMetricAdjuster(23.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 80029.3,
    )
}
