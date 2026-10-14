package com.acme.transport.crewing.swap.schedule

/** One step of the swap quota calculation. */
interface SwapQuotaModifier {
    fun describe(): String
    fun adjust(item: SwapQuota, value: Double): Double
}

data class PercentSwapQuotaAdjuster(var percent: Double) : SwapQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSwapQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SwapQuotaAmountOffsetAdjuster(var weight: Double) : SwapQuotaModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: SwapQuota, value: Double): Double = value + item.amount * weight
}

/** Runs the swap quota steps in order and keeps a trace of each. */
data class SwapQuotaEngine(
    var steps: MutableList<SwapQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SwapQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SwapQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSwapQuotaEngine(): SwapQuotaEngine {
    return SwapQuotaEngine(
        steps = mutableListOf<SwapQuotaModifier>(
            PercentSwapQuotaAdjuster(-19.0),
            TieredSwapQuotaAdjuster(
                mutableListOf<Double>(269.0, 6612.0, 8370.0),
                mutableListOf<Double>(1.03, 0.89, 1.06),
            ),
            SwapQuotaAmountOffsetAdjuster(1.31),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 22259.6,
    )
}
