package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap draft calculation. */
interface SwapDraftModifier {
    fun describe(): String
    fun adjust(item: SwapDraft, value: Double): Double
}

data class PercentSwapDraftAdjuster(var percent: Double) : SwapDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SwapDraftRetriesOffsetAdjuster(var weight: Double) : SwapDraftModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: SwapDraft, value: Double): Double = value + item.retries.toDouble() * weight
}

data class CapSwapDraftAdjuster(var cap: Double) : SwapDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapDraft, value: Double): Double = minOf(value, cap)
}

data class NameSwapDraftAdjuster(var factors: MutableMap<String, Double>) : SwapDraftModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: SwapDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the swap draft steps in order and keeps a trace of each. */
data class SwapDraftEngine(
    var steps: MutableList<SwapDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SwapDraft): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSwapDraftEngine(): SwapDraftEngine {
    return SwapDraftEngine(
        steps = mutableListOf<SwapDraftModifier>(
            PercentSwapDraftAdjuster(-0.6),
            SwapDraftRetriesOffsetAdjuster(1.27),
            CapSwapDraftAdjuster(27834.0),
            NameSwapDraftAdjuster(
                mutableMapOf<String, Double>("south" to 1.21, "online" to 0.7, "north" to 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 12737.4,
    )
}
