package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered cutoff adjustment calculation. */
interface TieredCutoffAdjustmentModifier {
    fun describe(): String
    fun adjust(item: TieredCutoffAdjustment, value: Double): Double
}

data class FlaggedTieredCutoffAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCutoffAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCutoffAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCutoffAdjustmentAmountOffsetAdjuster(var weight: Double) : TieredCutoffAdjustmentModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: TieredCutoffAdjustment, value: Double): Double = value + item.amount * weight
}

data class PercentTieredCutoffAdjustmentAdjuster(var percent: Double) : TieredCutoffAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCutoffAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredCutoffAdjustmentAdjuster(var step: Double) : TieredCutoffAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCutoffAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered cutoff adjustment steps in order and keeps a trace of each. */
data class TieredCutoffAdjustmentEngine(
    var steps: MutableList<TieredCutoffAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredCutoffAdjustment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredCutoffAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredCutoffAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredCutoffAdjustmentEngine(flags: Registry): TieredCutoffAdjustmentEngine {
    return TieredCutoffAdjustmentEngine(
        steps = mutableListOf<TieredCutoffAdjustmentModifier>(
            FlaggedTieredCutoffAdjustmentAdjuster(flags, "tiered-cutoff-adjustment-batched", 7.8),
            TieredCutoffAdjustmentAmountOffsetAdjuster(0.88),
            PercentTieredCutoffAdjustmentAdjuster(-0.8),
            RoundingTieredCutoffAdjustmentAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 52209.7,
    )
}
