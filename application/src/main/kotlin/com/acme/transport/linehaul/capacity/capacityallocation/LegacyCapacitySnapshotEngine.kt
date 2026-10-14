package com.acme.transport.linehaul.capacity.capacityallocation

/** One step of the legacy capacity snapshot calculation. */
interface LegacyCapacitySnapshotModifier {
    fun describe(): String
    fun adjust(item: LegacyCapacitySnapshot, value: Double): Double
}

data class NameLegacyCapacitySnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyCapacitySnapshotModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyCapacitySnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyCapacitySnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyCapacitySnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyCapacitySnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLegacyCapacitySnapshotAdjuster(var minimum: Double) : LegacyCapacitySnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyCapacitySnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy capacity snapshot steps in order and keeps a trace of each. */
data class LegacyCapacitySnapshotEngine(
    var steps: MutableList<LegacyCapacitySnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyCapacitySnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyCapacitySnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyCapacitySnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyCapacitySnapshotEngine(): LegacyCapacitySnapshotEngine {
    return LegacyCapacitySnapshotEngine(
        steps = mutableListOf<LegacyCapacitySnapshotModifier>(
            NameLegacyCapacitySnapshotAdjuster(mutableMapOf<String, Double>("west" to 0.82)),
            TieredLegacyCapacitySnapshotAdjuster(
                mutableListOf<Double>(5710.0, 5957.0),
                mutableListOf<Double>(0.87, 0.95),
            ),
            MinimumLegacyCapacitySnapshotAdjuster(48.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 64588.9,
    )
}
