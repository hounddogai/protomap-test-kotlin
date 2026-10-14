package com.acme.transport.customs.inspection.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy inspection forecast calculation. */
interface LegacyInspectionForecastAdjuster {
    fun describe(): String
    fun adjust(item: LegacyInspectionForecast, value: Double): Double
}

data class PercentLegacyInspectionForecastAdjuster(var percent: Double) : LegacyInspectionForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyInspectionForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LegacyInspectionForecastPalletsOffsetAdjuster(
    var weight: Double,
) : LegacyInspectionForecastAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: LegacyInspectionForecast, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class FlaggedLegacyInspectionForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyInspectionForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyInspectionForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy inspection forecast steps in order and keeps a trace of each. */
data class LegacyInspectionForecastEngine(
    var steps: MutableList<LegacyInspectionForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyInspectionForecast): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyInspectionForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyInspectionForecastEngine(flags: Registry): LegacyInspectionForecastEngine {
    return LegacyInspectionForecastEngine(
        steps = mutableListOf<LegacyInspectionForecastAdjuster>(
            PercentLegacyInspectionForecastAdjuster(16.5),
            LegacyInspectionForecastPalletsOffsetAdjuster(0.83),
            FlaggedLegacyInspectionForecastAdjuster(flags, "legacy-inspection-forecast-default", 0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 45477.0,
    )
}
