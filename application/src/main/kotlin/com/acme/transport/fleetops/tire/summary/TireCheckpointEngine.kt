package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tire checkpoint calculation. */
interface TireCheckpointModifier {
    fun describe(): String
    fun adjust(item: TireCheckpoint, value: Double): Double
}

data class LabelTireCheckpointAdjuster(var factors: MutableMap<String, Double>) : TireCheckpointModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TireCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTireCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireCheckpointModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTireCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTireCheckpointAdjuster(var step: Double) : TireCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentTireCheckpointAdjuster(var percent: Double) : TireCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTireCheckpointAdjuster(var cap: Double) : TireCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireCheckpoint, value: Double): Double = minOf(value, cap)
}

/** Runs the tire checkpoint steps in order and keeps a trace of each. */
data class TireCheckpointEngine(
    var steps: MutableList<TireCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireCheckpoint): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TireCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireCheckpointEngine(flags: Registry): TireCheckpointEngine {
    return TireCheckpointEngine(
        steps = mutableListOf<TireCheckpointModifier>(
            LabelTireCheckpointAdjuster(mutableMapOf<String, Double>("west" to 1.15, "south" to 1.11)),
            FlaggedTireCheckpointAdjuster(flags, "tire-checkpoint-default", -5.2),
            TieredTireCheckpointAdjuster(
                mutableListOf<Double>(461.0, 7998.0, 9362.0),
                mutableListOf<Double>(1.16, 1.03, 0.81),
            ),
            RoundingTireCheckpointAdjuster(0.05),
            PercentTireCheckpointAdjuster(7.0),
            CapTireCheckpointAdjuster(6899.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 19039.0,
    )
}
