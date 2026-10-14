package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered cross dock baseline calculation. */
interface TieredCrossDockBaselineStep {
    fun describe(): String
    fun adjust(item: TieredCrossDockBaseline, value: Double): Double
}

data class RoundingTieredCrossDockBaselineAdjuster(var step: Double) : TieredCrossDockBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCrossDockBaseline, value: Double): Double = roundTo(value, step)
}

data class PercentTieredCrossDockBaselineAdjuster(var percent: Double) : TieredCrossDockBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCrossDockBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCrossDockBaselineStartsAtOffsetAdjuster(var weight: Double) : TieredCrossDockBaselineStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: TieredCrossDockBaseline, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class FlaggedTieredCrossDockBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCrossDockBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCrossDockBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceTieredCrossDockBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredCrossDockBaselineStep {
    override fun describe(): String = "by source"

    override fun adjust(item: TieredCrossDockBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTieredCrossDockBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCrossDockBaselineStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCrossDockBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered cross dock baseline steps in order and keeps a trace of each. */
data class TieredCrossDockBaselineEngine(
    var steps: MutableList<TieredCrossDockBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredCrossDockBaseline): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredCrossDockBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredCrossDockBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredCrossDockBaselineEngine(flags: Registry): TieredCrossDockBaselineEngine {
    return TieredCrossDockBaselineEngine(
        steps = mutableListOf<TieredCrossDockBaselineStep>(
            RoundingTieredCrossDockBaselineAdjuster(0.05),
            PercentTieredCrossDockBaselineAdjuster(-24.8),
            TieredCrossDockBaselineStartsAtOffsetAdjuster(0.84),
            FlaggedTieredCrossDockBaselineAdjuster(flags, "tiered-cross-dock-baseline-batched", 9.6),
            SourceTieredCrossDockBaselineAdjuster(
                mutableMapOf<String, Double>("east" to 0.86, "central" to 1.37, "online" to 0.98),
            ),
            TieredTieredCrossDockBaselineAdjuster(
                mutableListOf<Double>(39.0, 4154.0, 4759.0, 9907.0),
                mutableListOf<Double>(0.94, 1.2, 1.08, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 56496.7,
    )
}
