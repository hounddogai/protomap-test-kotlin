package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap policy calculation. */
interface SwapPolicyModifier {
    fun describe(): String
    fun adjust(item: SwapPolicy, value: Double): Double
}

data class PercentSwapPolicyAdjuster(var percent: Double) : SwapPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelSwapPolicyAdjuster(var factors: MutableMap<String, Double>) : SwapPolicyModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: SwapPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SwapPolicyCostOffsetAdjuster(var weight: Double) : SwapPolicyModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: SwapPolicy, value: Double): Double = value + item.cost * weight
}

data class RoundingSwapPolicyAdjuster(var step: Double) : SwapPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the swap policy steps in order and keeps a trace of each. */
data class SwapPolicyEngine(
    var steps: MutableList<SwapPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SwapPolicy): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSwapPolicyEngine(): SwapPolicyEngine {
    return SwapPolicyEngine(
        steps = mutableListOf<SwapPolicyModifier>(
            PercentSwapPolicyAdjuster(3.8),
            LabelSwapPolicyAdjuster(mutableMapOf<String, Double>("east" to 0.99)),
            SwapPolicyCostOffsetAdjuster(1.48),
            RoundingSwapPolicyAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 84215.5,
    )
}
