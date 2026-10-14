package com.acme.transport.customs.inspection.window

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling inspection milestone calculation. */
interface RollingInspectionMilestoneFactor {
    fun describe(): String
    fun adjust(item: RollingInspectionMilestone, value: Double): Double
}

data class LabelRollingInspectionMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingInspectionMilestoneFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingInspectionMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingInspectionMilestoneAdjuster(var percent: Double) : RollingInspectionMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingInspectionMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingInspectionMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingInspectionMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingInspectionMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingInspectionMilestoneCapacityOffsetAdjuster(
    var weight: Double,
) : RollingInspectionMilestoneFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RollingInspectionMilestone, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

/** Runs the rolling inspection milestone steps in order and keeps a trace of each. */
data class RollingInspectionMilestoneEngine(
    var steps: MutableList<RollingInspectionMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingInspectionMilestone): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingInspectionMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingInspectionMilestoneEngine(): RollingInspectionMilestoneEngine {
    return RollingInspectionMilestoneEngine(
        steps = mutableListOf<RollingInspectionMilestoneFactor>(
            LabelRollingInspectionMilestoneAdjuster(mutableMapOf<String, Double>("south" to 0.89)),
            PercentRollingInspectionMilestoneAdjuster(-20.8),
            TieredRollingInspectionMilestoneAdjuster(
                mutableListOf<Double>(3208.0, 5719.0),
                mutableListOf<Double>(0.94, 1.19),
            ),
            RollingInspectionMilestoneCapacityOffsetAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 30185.0,
    )
}
