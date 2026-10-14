package com.acme.transport.fleetops.inspection.mapping

/** One step of the capped inspection mapping calculation. */
interface CappedInspectionMappingAdjuster {
    fun describe(): String
    fun adjust(item: CappedInspectionMapping, value: Double): Double
}

data class MinimumCappedInspectionMappingAdjuster(var minimum: Double) : CappedInspectionMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedInspectionMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedInspectionMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedInspectionMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedInspectionMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleCappedInspectionMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedInspectionMappingAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedInspectionMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCappedInspectionMappingAdjuster(var percent: Double) : CappedInspectionMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedInspectionMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped inspection mapping steps in order and keeps a trace of each. */
data class CappedInspectionMappingEngine(
    var steps: MutableList<CappedInspectionMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedInspectionMapping): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedInspectionMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedInspectionMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedInspectionMappingEngine(): CappedInspectionMappingEngine {
    return CappedInspectionMappingEngine(
        steps = mutableListOf<CappedInspectionMappingAdjuster>(
            MinimumCappedInspectionMappingAdjuster(0.5),
            TieredCappedInspectionMappingAdjuster(
                mutableListOf<Double>(3065.0, 5136.0, 6844.0, 7102.0),
                mutableListOf<Double>(0.82, 0.97, 1.1, 1.04),
            ),
            TitleCappedInspectionMappingAdjuster(mutableMapOf<String, Double>("central" to 0.8)),
            PercentCappedInspectionMappingAdjuster(11.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 36112.1,
    )
}
