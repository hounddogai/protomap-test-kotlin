package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.numbers.roundTo

/** One step of the near miss checkpoint calculation. */
interface NearMissCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: NearMissCheckpoint, value: Double): Double
}

data class LabelNearMissCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : NearMissCheckpointAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: NearMissCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumNearMissCheckpointAdjuster(var minimum: Double) : NearMissCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapNearMissCheckpointAdjuster(var cap: Double) : NearMissCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RoundingNearMissCheckpointAdjuster(var step: Double) : NearMissCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentNearMissCheckpointAdjuster(var percent: Double) : NearMissCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredNearMissCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the near miss checkpoint steps in order and keeps a trace of each. */
data class NearMissCheckpointEngine(
    var steps: MutableList<NearMissCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NearMissCheckpoint): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNearMissCheckpointEngine(): NearMissCheckpointEngine {
    return NearMissCheckpointEngine(
        steps = mutableListOf<NearMissCheckpointAdjuster>(
            LabelNearMissCheckpointAdjuster(
                mutableMapOf<String, Double>("south" to 0.78, "north" to 1.38, "east" to 0.99),
            ),
            MinimumNearMissCheckpointAdjuster(14.1),
            CapNearMissCheckpointAdjuster(49323.0),
            RoundingNearMissCheckpointAdjuster(5.0),
            PercentNearMissCheckpointAdjuster(-17.4),
            TieredNearMissCheckpointAdjuster(
                mutableListOf<Double>(2998.0, 6144.0, 8665.0),
                mutableListOf<Double>(0.98, 1.04, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 57980.9,
    )
}
