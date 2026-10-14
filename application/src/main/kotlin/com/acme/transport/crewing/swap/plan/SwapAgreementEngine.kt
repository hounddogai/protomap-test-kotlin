package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap agreement calculation. */
interface SwapAgreementFactor {
    fun describe(): String
    fun adjust(item: SwapAgreement, value: Double): Double
}

data class RoundingSwapAgreementAdjuster(var step: Double) : SwapAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapAgreement, value: Double): Double = roundTo(value, step)
}

data class PercentSwapAgreementAdjuster(var percent: Double) : SwapAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentSwapAgreementAdjuster(var factors: MutableMap<String, Double>) : SwapAgreementFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: SwapAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSwapAgreementAdjuster(var minimum: Double) : SwapAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSwapAgreementAdjuster(var cap: Double) : SwapAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapAgreement, value: Double): Double = minOf(value, cap)
}

data class SwapAgreementSequenceOffsetAdjuster(var weight: Double) : SwapAgreementFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: SwapAgreement, value: Double): Double = value + item.sequence.toDouble() * weight
}

/** Runs the swap agreement steps in order and keeps a trace of each. */
data class SwapAgreementEngine(
    var steps: MutableList<SwapAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SwapAgreement): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSwapAgreementEngine(): SwapAgreementEngine {
    return SwapAgreementEngine(
        steps = mutableListOf<SwapAgreementFactor>(
            RoundingSwapAgreementAdjuster(0.1),
            PercentSwapAgreementAdjuster(3.6),
            SegmentSwapAgreementAdjuster(mutableMapOf<String, Double>("west" to 0.95)),
            MinimumSwapAgreementAdjuster(29.0),
            CapSwapAgreementAdjuster(16928.0),
            SwapAgreementSequenceOffsetAdjuster(0.24),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 2098.2,
    )
}
