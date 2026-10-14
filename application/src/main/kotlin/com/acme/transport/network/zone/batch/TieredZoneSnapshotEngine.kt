package com.acme.transport.network.zone.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered zone snapshot calculation. */
interface TieredZoneSnapshotFactor {
    fun describe(): String
    fun adjust(item: TieredZoneSnapshot, value: Double): Double
}

data class PercentTieredZoneSnapshotAdjuster(var percent: Double) : TieredZoneSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredZoneSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredZoneSnapshotAdjuster(var cap: Double) : TieredZoneSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredZoneSnapshot, value: Double): Double = minOf(value, cap)
}

data class TieredZoneSnapshotOdometerOffsetAdjuster(var weight: Double) : TieredZoneSnapshotFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: TieredZoneSnapshot, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class TieredTieredZoneSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredZoneSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredZoneSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredZoneSnapshotAdjuster(var step: Double) : TieredZoneSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredZoneSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered zone snapshot steps in order and keeps a trace of each. */
data class TieredZoneSnapshotEngine(
    var steps: MutableList<TieredZoneSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredZoneSnapshot): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredZoneSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredZoneSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredZoneSnapshotEngine(): TieredZoneSnapshotEngine {
    return TieredZoneSnapshotEngine(
        steps = mutableListOf<TieredZoneSnapshotFactor>(
            PercentTieredZoneSnapshotAdjuster(-17.9),
            CapTieredZoneSnapshotAdjuster(43481.0),
            TieredZoneSnapshotOdometerOffsetAdjuster(0.47),
            TieredTieredZoneSnapshotAdjuster(
                mutableListOf<Double>(271.0, 4140.0),
                mutableListOf<Double>(1.13, 1.1),
            ),
            RoundingTieredZoneSnapshotAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 17445.6,
    )
}
