package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional zone checkpoint calculation. */
interface RegionalZoneCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: RegionalZoneCheckpoint, value: Double): Double
}

data class RoundingRegionalZoneCheckpointAdjuster(var step: Double) : RegionalZoneCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalZoneCheckpoint, value: Double): Double = roundTo(value, step)
}

data class CapRegionalZoneCheckpointAdjuster(var cap: Double) : RegionalZoneCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalZoneCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalZoneCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalZoneCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalZoneCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalZoneCheckpointAdjuster(var percent: Double) : RegionalZoneCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalZoneCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional zone checkpoint steps in order and keeps a trace of each. */
data class RegionalZoneCheckpointEngine(
    var steps: MutableList<RegionalZoneCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalZoneCheckpoint): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalZoneCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalZoneCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalZoneCheckpointEngine(): RegionalZoneCheckpointEngine {
    return RegionalZoneCheckpointEngine(
        steps = mutableListOf<RegionalZoneCheckpointAdjuster>(
            RoundingRegionalZoneCheckpointAdjuster(1.0),
            CapRegionalZoneCheckpointAdjuster(4345.0),
            TieredRegionalZoneCheckpointAdjuster(
                mutableListOf<Double>(465.0, 3566.0, 4688.0),
                mutableListOf<Double>(0.86, 0.87, 0.89),
            ),
            PercentRegionalZoneCheckpointAdjuster(-5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 35696.6,
    )
}
