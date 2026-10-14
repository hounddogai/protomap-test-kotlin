package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the cross dock rollup calculation. */
interface CrossDockRollupFactor {
    fun describe(): String
    fun adjust(item: CrossDockRollup, value: Double): Double
}

data class FlaggedCrossDockRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentCrossDockRollupAdjuster(var factors: MutableMap<String, Double>) : CrossDockRollupFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: CrossDockRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCrossDockRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCrossDockRollupAdjuster(var cap: Double) : CrossDockRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockRollup, value: Double): Double = minOf(value, cap)
}

data class PercentCrossDockRollupAdjuster(var percent: Double) : CrossDockRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CrossDockRollupBatchSizeOffsetAdjuster(var weight: Double) : CrossDockRollupFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: CrossDockRollup, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the cross dock rollup steps in order and keeps a trace of each. */
data class CrossDockRollupEngine(
    var steps: MutableList<CrossDockRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockRollupEngine(flags: Registry): CrossDockRollupEngine {
    return CrossDockRollupEngine(
        steps = mutableListOf<CrossDockRollupFactor>(
            FlaggedCrossDockRollupAdjuster(flags, "cross-dock-rollup-weighted", 6.8),
            SegmentCrossDockRollupAdjuster(mutableMapOf<String, Double>("east" to 1.1)),
            TieredCrossDockRollupAdjuster(
                mutableListOf<Double>(188.0, 685.0, 4793.0, 6393.0),
                mutableListOf<Double>(1.08, 1.13, 1.0, 0.81),
            ),
            CapCrossDockRollupAdjuster(13043.0),
            PercentCrossDockRollupAdjuster(-17.9),
            CrossDockRollupBatchSizeOffsetAdjuster(1.18),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 71270.5,
    )
}
