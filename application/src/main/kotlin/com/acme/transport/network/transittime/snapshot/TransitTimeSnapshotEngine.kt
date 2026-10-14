package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time snapshot calculation. */
interface TransitTimeSnapshotFactor {
    fun describe(): String
    fun adjust(item: TransitTimeSnapshot, value: Double): Double
}

data class RoundingTransitTimeSnapshotAdjuster(var step: Double) : TransitTimeSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeSnapshot, value: Double): Double = roundTo(value, step)
}

data class DepotTransitTimeSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : TransitTimeSnapshotFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: TransitTimeSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTransitTimeSnapshotAdjuster(var minimum: Double) : TransitTimeSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTransitTimeSnapshotAdjuster(var cap: Double) : TransitTimeSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeSnapshot, value: Double): Double = minOf(value, cap)
}

data class PercentTransitTimeSnapshotAdjuster(var percent: Double) : TransitTimeSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the transit time snapshot steps in order and keeps a trace of each. */
data class TransitTimeSnapshotEngine(
    var steps: MutableList<TransitTimeSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeSnapshot): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeSnapshotEngine(): TransitTimeSnapshotEngine {
    return TransitTimeSnapshotEngine(
        steps = mutableListOf<TransitTimeSnapshotFactor>(
            RoundingTransitTimeSnapshotAdjuster(5.0),
            DepotTransitTimeSnapshotAdjuster(mutableMapOf<String, Double>("south" to 0.91)),
            MinimumTransitTimeSnapshotAdjuster(24.0),
            CapTransitTimeSnapshotAdjuster(13917.0),
            PercentTransitTimeSnapshotAdjuster(-14.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 46414.2,
    )
}
