package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap variance calculation. */
interface SwapVarianceAdjuster {
    fun describe(): String
    fun adjust(item: SwapVariance, value: Double): Double
}

data class FlaggedSwapVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SwapVarianceMarginOffsetAdjuster(var weight: Double) : SwapVarianceAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: SwapVariance, value: Double): Double = value + item.margin * weight
}

data class RoundingSwapVarianceAdjuster(var step: Double) : SwapVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapVariance, value: Double): Double = roundTo(value, step)
}

data class PercentSwapVarianceAdjuster(var percent: Double) : SwapVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleSwapVarianceAdjuster(var factors: MutableMap<String, Double>) : SwapVarianceAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: SwapVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSwapVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the swap variance steps in order and keeps a trace of each. */
data class SwapVarianceEngine(
    var steps: MutableList<SwapVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapVariance): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapVarianceEngine(flags: Registry): SwapVarianceEngine {
    return SwapVarianceEngine(
        steps = mutableListOf<SwapVarianceAdjuster>(
            FlaggedSwapVarianceAdjuster(flags, "swap-variance-adaptive", 9.1),
            SwapVarianceMarginOffsetAdjuster(1.38),
            RoundingSwapVarianceAdjuster(0.5),
            PercentSwapVarianceAdjuster(-4.0),
            TitleSwapVarianceAdjuster(mutableMapOf<String, Double>("south" to 0.83, "north" to 1.4)),
            TieredSwapVarianceAdjuster(
                mutableListOf<Double>(1289.0, 6690.0, 7516.0),
                mutableListOf<Double>(1.11, 1.03, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 9178.5,
    )
}
