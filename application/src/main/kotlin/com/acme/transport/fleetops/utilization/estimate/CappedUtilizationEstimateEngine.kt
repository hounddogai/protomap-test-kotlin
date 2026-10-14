package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped utilization estimate calculation. */
interface CappedUtilizationEstimateModifier {
    fun describe(): String
    fun adjust(item: CappedUtilizationEstimate, value: Double): Double
}

data class CategoryCappedUtilizationEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedUtilizationEstimateModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedUtilizationEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedUtilizationEstimateAdjuster(var cap: Double) : CappedUtilizationEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedUtilizationEstimate, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedUtilizationEstimateAdjuster(var minimum: Double) : CappedUtilizationEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedUtilizationEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedUtilizationEstimateAdjuster(var percent: Double) : CappedUtilizationEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedUtilizationEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped utilization estimate steps in order and keeps a trace of each. */
data class CappedUtilizationEstimateEngine(
    var steps: MutableList<CappedUtilizationEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedUtilizationEstimate): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedUtilizationEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedUtilizationEstimateEngine(): CappedUtilizationEstimateEngine {
    return CappedUtilizationEstimateEngine(
        steps = mutableListOf<CappedUtilizationEstimateModifier>(
            CategoryCappedUtilizationEstimateAdjuster(
                mutableMapOf<String, Double>("south" to 1.05, "retail" to 1.17, "north" to 1.07),
            ),
            CapCappedUtilizationEstimateAdjuster(25929.0),
            MinimumCappedUtilizationEstimateAdjuster(35.0),
            PercentCappedUtilizationEstimateAdjuster(-14.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 60022.5,
    )
}
