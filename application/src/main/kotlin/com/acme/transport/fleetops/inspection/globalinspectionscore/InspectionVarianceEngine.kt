package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.flags.Registry

/** One step of the inspection variance calculation. */
interface InspectionVarianceModifier {
    fun describe(): String
    fun adjust(item: InspectionVariance, value: Double): Double
}

data class CapInspectionVarianceAdjuster(var cap: Double) : InspectionVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionVariance, value: Double): Double = minOf(value, cap)
}

data class TitleInspectionVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionVarianceModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: InspectionVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedInspectionVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the inspection variance steps in order and keeps a trace of each. */
data class InspectionVarianceEngine(
    var steps: MutableList<InspectionVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionVarianceEngine(flags: Registry): InspectionVarianceEngine {
    return InspectionVarianceEngine(
        steps = mutableListOf<InspectionVarianceModifier>(
            CapInspectionVarianceAdjuster(16082.0),
            TitleInspectionVarianceAdjuster(mutableMapOf<String, Double>("central" to 1.2)),
            FlaggedInspectionVarianceAdjuster(flags, "inspection-variance-scheduled", -6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 50354.0,
    )
}
