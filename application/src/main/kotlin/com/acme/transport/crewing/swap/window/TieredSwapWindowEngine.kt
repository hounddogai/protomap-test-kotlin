package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered swap window calculation. */
interface TieredSwapWindowAdjuster {
    fun describe(): String
    fun adjust(item: TieredSwapWindow, value: Double): Double
}

data class TieredTieredSwapWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredSwapWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredSwapWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelTieredSwapWindowAdjuster(var factors: MutableMap<String, Double>) : TieredSwapWindowAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredSwapWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredSwapWindowAdjuster(var minimum: Double) : TieredSwapWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredSwapWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredSwapWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredSwapWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredSwapWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTieredSwapWindowAdjuster(var step: Double) : TieredSwapWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredSwapWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered swap window steps in order and keeps a trace of each. */
data class TieredSwapWindowEngine(
    var steps: MutableList<TieredSwapWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredSwapWindow): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredSwapWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredSwapWindowEngine(flags: Registry): TieredSwapWindowEngine {
    return TieredSwapWindowEngine(
        steps = mutableListOf<TieredSwapWindowAdjuster>(
            TieredTieredSwapWindowAdjuster(
                mutableListOf<Double>(3104.0, 4586.0, 4855.0),
                mutableListOf<Double>(0.86, 1.11, 0.98),
            ),
            LabelTieredSwapWindowAdjuster(mutableMapOf<String, Double>("central" to 1.06, "south" to 1.22)),
            MinimumTieredSwapWindowAdjuster(8.4),
            FlaggedTieredSwapWindowAdjuster(flags, "tiered-swap-window-batched", -4.5),
            RoundingTieredSwapWindowAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 96444.3,
    )
}
