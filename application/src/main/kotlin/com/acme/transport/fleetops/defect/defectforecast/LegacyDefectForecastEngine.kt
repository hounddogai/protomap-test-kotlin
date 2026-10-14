package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.flags.Registry

/** One step of the legacy defect forecast calculation. */
interface LegacyDefectForecastModifier {
    fun describe(): String
    fun adjust(item: LegacyDefectForecast, value: Double): Double
}

data class FlaggedLegacyDefectForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyDefectForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyDefectForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneLegacyDefectForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDefectForecastModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: LegacyDefectForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyDefectForecastAdjuster(var cap: Double) : LegacyDefectForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDefectForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy defect forecast steps in order and keeps a trace of each. */
data class LegacyDefectForecastEngine(
    var steps: MutableList<LegacyDefectForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDefectForecast): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyDefectForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyDefectForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDefectForecastEngine(flags: Registry): LegacyDefectForecastEngine {
    return LegacyDefectForecastEngine(
        steps = mutableListOf<LegacyDefectForecastModifier>(
            FlaggedLegacyDefectForecastAdjuster(flags, "legacy-defect-forecast-legacy", 8.0),
            LaneLegacyDefectForecastAdjuster(mutableMapOf<String, Double>("north" to 0.88, "central" to 1.24)),
            CapLegacyDefectForecastAdjuster(9062.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 91212.6,
    )
}
