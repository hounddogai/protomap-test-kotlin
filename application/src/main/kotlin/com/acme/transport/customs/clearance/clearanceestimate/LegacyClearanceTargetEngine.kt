package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy clearance target calculation. */
interface LegacyClearanceTargetFactor {
    fun describe(): String
    fun adjust(item: LegacyClearanceTarget, value: Double): Double
}

data class LegacyClearanceTargetPalletsOffsetAdjuster(var weight: Double) : LegacyClearanceTargetFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: LegacyClearanceTarget, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class LocaleLegacyClearanceTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyClearanceTargetFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: LegacyClearanceTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyClearanceTargetAdjuster(var step: Double) : LegacyClearanceTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyClearanceTarget, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyClearanceTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyClearanceTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyClearanceTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy clearance target steps in order and keeps a trace of each. */
data class LegacyClearanceTargetEngine(
    var steps: MutableList<LegacyClearanceTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyClearanceTarget): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyClearanceTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyClearanceTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyClearanceTargetEngine(): LegacyClearanceTargetEngine {
    return LegacyClearanceTargetEngine(
        steps = mutableListOf<LegacyClearanceTargetFactor>(
            LegacyClearanceTargetPalletsOffsetAdjuster(1.49),
            LocaleLegacyClearanceTargetAdjuster(mutableMapOf<String, Double>("online" to 1.35, "east" to 1.36)),
            RoundingLegacyClearanceTargetAdjuster(5.0),
            TieredLegacyClearanceTargetAdjuster(
                mutableListOf<Double>(1835.0, 3857.0, 6845.0, 7698.0),
                mutableListOf<Double>(1.14, 1.01, 0.99, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 65991.1,
    )
}
