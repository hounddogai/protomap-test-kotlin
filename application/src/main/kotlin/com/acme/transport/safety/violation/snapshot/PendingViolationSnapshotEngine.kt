package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending violation snapshot calculation. */
interface PendingViolationSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: PendingViolationSnapshot, value: Double): Double
}

data class PendingViolationSnapshotFuelLitersOffsetAdjuster(
    var weight: Double,
) : PendingViolationSnapshotAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: PendingViolationSnapshot, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredPendingViolationSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingViolationSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingViolationSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelPendingViolationSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingViolationSnapshotAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: PendingViolationSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingViolationSnapshotAdjuster(var step: Double) : PendingViolationSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingViolationSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the pending violation snapshot steps in order and keeps a trace of each. */
data class PendingViolationSnapshotEngine(
    var steps: MutableList<PendingViolationSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingViolationSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingViolationSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingViolationSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingViolationSnapshotEngine(): PendingViolationSnapshotEngine {
    return PendingViolationSnapshotEngine(
        steps = mutableListOf<PendingViolationSnapshotAdjuster>(
            PendingViolationSnapshotFuelLitersOffsetAdjuster(0.03),
            TieredPendingViolationSnapshotAdjuster(
                mutableListOf<Double>(347.0, 2062.0, 7927.0),
                mutableListOf<Double>(1.12, 1.17, 0.99),
            ),
            ChannelPendingViolationSnapshotAdjuster(
                mutableMapOf<String, Double>("west" to 0.99, "north" to 1.13, "central" to 1.11),
            ),
            RoundingPendingViolationSnapshotAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 57903.9,
    )
}
