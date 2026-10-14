package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the manual violation forecast calculation. */
interface ManualViolationForecastModifier {
    fun describe(): String
    fun adjust(item: ManualViolationForecast, value: Double): Double
}

data class PercentManualViolationForecastAdjuster(var percent: Double) : ManualViolationForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualViolationForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualViolationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualViolationForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualViolationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualViolationForecastAdjuster(var cap: Double) : ManualViolationForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualViolationForecast, value: Double): Double = minOf(value, cap)
}

data class LabelManualViolationForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualViolationForecastModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualViolationForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual violation forecast steps in order and keeps a trace of each. */
data class ManualViolationForecastEngine(
    var steps: MutableList<ManualViolationForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualViolationForecast): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualViolationForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualViolationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualViolationForecastEngine(flags: Registry): ManualViolationForecastEngine {
    return ManualViolationForecastEngine(
        steps = mutableListOf<ManualViolationForecastModifier>(
            PercentManualViolationForecastAdjuster(14.0),
            FlaggedManualViolationForecastAdjuster(flags, "manual-violation-forecast-pending", -0.7),
            CapManualViolationForecastAdjuster(35471.0),
            LabelManualViolationForecastAdjuster(mutableMapOf<String, Double>("central" to 0.96)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 59746.9,
    )
}
