package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop adjustment calculation. */
interface DropAdjustmentStep {
    fun describe(): String
    fun adjust(item: DropAdjustment, value: Double): Double
}

data class TieredDropAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDropAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDropAdjustmentAdjuster(var step: Double) : DropAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the drop adjustment steps in order and keeps a trace of each. */
data class DropAdjustmentEngine(
    var steps: MutableList<DropAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropAdjustment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DropAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropAdjustmentEngine(flags: Registry): DropAdjustmentEngine {
    return DropAdjustmentEngine(
        steps = mutableListOf<DropAdjustmentStep>(
            TieredDropAdjustmentAdjuster(
                mutableListOf<Double>(189.0, 2837.0, 6735.0, 7584.0),
                mutableListOf<Double>(1.1, 0.89, 0.81, 1.05),
            ),
            FlaggedDropAdjustmentAdjuster(flags, "drop-adjustment-batched", 3.0),
            RoundingDropAdjustmentAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 57050.9,
    )
}
