package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the swap cycle calculation. */
interface SwapCycleFactor {
    fun describe(): String
    fun adjust(item: SwapCycle, value: Double): Double
}

data class FlaggedSwapCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSwapCycleAdjuster(var step: Double) : SwapCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapCycle, value: Double): Double = roundTo(value, step)
}

data class PercentSwapCycleAdjuster(var percent: Double) : SwapCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSwapCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSwapCycleAdjuster(var cap: Double) : SwapCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapCycle, value: Double): Double = minOf(value, cap)
}

data class LabelSwapCycleAdjuster(var factors: MutableMap<String, Double>) : SwapCycleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: SwapCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the swap cycle steps in order and keeps a trace of each. */
data class SwapCycleEngine(
    var steps: MutableList<SwapCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SwapCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SwapCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSwapCycleEngine(flags: Registry): SwapCycleEngine {
    return SwapCycleEngine(
        steps = mutableListOf<SwapCycleFactor>(
            FlaggedSwapCycleAdjuster(flags, "swap-cycle-seasonal", -4.2),
            RoundingSwapCycleAdjuster(5.0),
            PercentSwapCycleAdjuster(-2.3),
            TieredSwapCycleAdjuster(
                mutableListOf<Double>(2099.0, 2105.0, 6473.0),
                mutableListOf<Double>(1.18, 1.09, 1.11),
            ),
            CapSwapCycleAdjuster(3358.0),
            LabelSwapCycleAdjuster(mutableMapOf<String, Double>("east" to 1.21, "north" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 86741.7,
    )
}
