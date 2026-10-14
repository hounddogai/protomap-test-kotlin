package com.acme.transport.fleetops.inspection.inspectionsummary

/** One step of the rolling inspection variance calculation. */
interface RollingInspectionVarianceFactor {
    fun describe(): String
    fun adjust(item: RollingInspectionVariance, value: Double): Double
}

data class CountryRollingInspectionVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingInspectionVarianceFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: RollingInspectionVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingInspectionVarianceWindowDaysOffsetAdjuster(
    var weight: Double,
) : RollingInspectionVarianceFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: RollingInspectionVariance, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class CapRollingInspectionVarianceAdjuster(var cap: Double) : RollingInspectionVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingInspectionVariance, value: Double): Double = minOf(value, cap)
}

data class PercentRollingInspectionVarianceAdjuster(var percent: Double) : RollingInspectionVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingInspectionVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling inspection variance steps in order and keeps a trace of each. */
data class RollingInspectionVarianceEngine(
    var steps: MutableList<RollingInspectionVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingInspectionVariance): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingInspectionVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingInspectionVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingInspectionVarianceEngine(): RollingInspectionVarianceEngine {
    return RollingInspectionVarianceEngine(
        steps = mutableListOf<RollingInspectionVarianceFactor>(
            CountryRollingInspectionVarianceAdjuster(
                mutableMapOf<String, Double>("south" to 0.99, "east" to 1.33, "central" to 0.88),
            ),
            RollingInspectionVarianceWindowDaysOffsetAdjuster(0.98),
            CapRollingInspectionVarianceAdjuster(2591.0),
            PercentRollingInspectionVarianceAdjuster(-21.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 15482.5,
    )
}
