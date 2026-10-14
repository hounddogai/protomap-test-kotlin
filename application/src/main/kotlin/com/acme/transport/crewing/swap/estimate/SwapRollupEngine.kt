package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap rollup calculation. */
interface SwapRollupModifier {
    fun describe(): String
    fun adjust(item: SwapRollup, value: Double): Double
}

data class FlaggedSwapRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSwapRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelSwapRollupAdjuster(var factors: MutableMap<String, Double>) : SwapRollupModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: SwapRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the swap rollup steps in order and keeps a trace of each. */
data class SwapRollupEngine(
    var steps: MutableList<SwapRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapRollup): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapRollupEngine(flags: Registry): SwapRollupEngine {
    return SwapRollupEngine(
        steps = mutableListOf<SwapRollupModifier>(
            FlaggedSwapRollupAdjuster(flags, "swap-rollup-weighted", 7.7),
            TieredSwapRollupAdjuster(
                mutableListOf<Double>(273.0, 4618.0, 5475.0, 8012.0),
                mutableListOf<Double>(0.89, 1.01, 0.83, 0.99),
            ),
            ChannelSwapRollupAdjuster(mutableMapOf<String, Double>("north" to 1.16)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 82349.2,
    )
}
