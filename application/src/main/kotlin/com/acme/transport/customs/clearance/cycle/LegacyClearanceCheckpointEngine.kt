package com.acme.transport.customs.clearance.cycle

/** One step of the legacy clearance checkpoint calculation. */
interface LegacyClearanceCheckpointModifier {
    fun describe(): String
    fun adjust(item: LegacyClearanceCheckpoint, value: Double): Double
}

data class CapLegacyClearanceCheckpointAdjuster(var cap: Double) : LegacyClearanceCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyClearanceCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyClearanceCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyClearanceCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyClearanceCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameLegacyClearanceCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyClearanceCheckpointModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyClearanceCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyClearanceCheckpointAdjuster(var percent: Double) : LegacyClearanceCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyClearanceCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy clearance checkpoint steps in order and keeps a trace of each. */
data class LegacyClearanceCheckpointEngine(
    var steps: MutableList<LegacyClearanceCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyClearanceCheckpoint): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyClearanceCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyClearanceCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyClearanceCheckpointEngine(): LegacyClearanceCheckpointEngine {
    return LegacyClearanceCheckpointEngine(
        steps = mutableListOf<LegacyClearanceCheckpointModifier>(
            CapLegacyClearanceCheckpointAdjuster(597.0),
            TieredLegacyClearanceCheckpointAdjuster(
                mutableListOf<Double>(2133.0, 2146.0, 5218.0, 5645.0),
                mutableListOf<Double>(1.06, 0.84, 0.97, 1.1),
            ),
            NameLegacyClearanceCheckpointAdjuster(mutableMapOf<String, Double>("east" to 1.36)),
            PercentLegacyClearanceCheckpointAdjuster(-23.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 68243.0,
    )
}
