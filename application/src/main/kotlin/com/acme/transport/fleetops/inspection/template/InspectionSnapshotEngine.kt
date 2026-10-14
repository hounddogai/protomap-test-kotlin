package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection snapshot calculation. */
interface InspectionSnapshotFactor {
    fun describe(): String
    fun adjust(item: InspectionSnapshot, value: Double): Double
}

data class RoundingInspectionSnapshotAdjuster(var step: Double) : InspectionSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionSnapshot, value: Double): Double = roundTo(value, step)
}

data class MinimumInspectionSnapshotAdjuster(var minimum: Double) : InspectionSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentInspectionSnapshotAdjuster(var percent: Double) : InspectionSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitInspectionSnapshotAdjuster(var factors: MutableMap<String, Double>) : InspectionSnapshotFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: InspectionSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredInspectionSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the inspection snapshot steps in order and keeps a trace of each. */
data class InspectionSnapshotEngine(
    var steps: MutableList<InspectionSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionSnapshot): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionSnapshotEngine(): InspectionSnapshotEngine {
    return InspectionSnapshotEngine(
        steps = mutableListOf<InspectionSnapshotFactor>(
            RoundingInspectionSnapshotAdjuster(5.0),
            MinimumInspectionSnapshotAdjuster(5.2),
            PercentInspectionSnapshotAdjuster(-24.8),
            UnitInspectionSnapshotAdjuster(
                mutableMapOf<String, Double>("online" to 1.2, "west" to 0.8, "retail" to 0.72),
            ),
            TieredInspectionSnapshotAdjuster(
                mutableListOf<Double>(4075.0, 8821.0),
                mutableListOf<Double>(0.86, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 45816.0,
    )
}
