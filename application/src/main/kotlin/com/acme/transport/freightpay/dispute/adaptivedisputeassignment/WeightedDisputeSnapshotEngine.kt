package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted dispute snapshot calculation. */
interface WeightedDisputeSnapshotFactor {
    fun describe(): String
    fun adjust(item: WeightedDisputeSnapshot, value: Double): Double
}

data class MinimumWeightedDisputeSnapshotAdjuster(var minimum: Double) : WeightedDisputeSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDisputeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedDisputeSnapshotAdjuster(var step: Double) : WeightedDisputeSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDisputeSnapshot, value: Double): Double = roundTo(value, step)
}

data class ChannelWeightedDisputeSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDisputeSnapshotFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: WeightedDisputeSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapWeightedDisputeSnapshotAdjuster(var cap: Double) : WeightedDisputeSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedDisputeSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedDisputeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDisputeSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDisputeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted dispute snapshot steps in order and keeps a trace of each. */
data class WeightedDisputeSnapshotEngine(
    var steps: MutableList<WeightedDisputeSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedDisputeSnapshot): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedDisputeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedDisputeSnapshotEngine(flags: Registry): WeightedDisputeSnapshotEngine {
    return WeightedDisputeSnapshotEngine(
        steps = mutableListOf<WeightedDisputeSnapshotFactor>(
            MinimumWeightedDisputeSnapshotAdjuster(39.3),
            RoundingWeightedDisputeSnapshotAdjuster(0.1),
            ChannelWeightedDisputeSnapshotAdjuster(
                mutableMapOf<String, Double>("south" to 0.95, "online" to 1.32, "central" to 1.17),
            ),
            CapWeightedDisputeSnapshotAdjuster(26768.0),
            FlaggedWeightedDisputeSnapshotAdjuster(flags, "weighted-dispute-snapshot-strict", 1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 75098.2,
    )
}
