package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive fuel burn estimate calculation. */
interface AdaptiveFuelBurnEstimateModifier {
    fun describe(): String
    fun adjust(item: AdaptiveFuelBurnEstimate, value: Double): Double
}

data class PercentAdaptiveFuelBurnEstimateAdjuster(var percent: Double) : AdaptiveFuelBurnEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveFuelBurnEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceAdaptiveFuelBurnEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveFuelBurnEstimateModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: AdaptiveFuelBurnEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveFuelBurnEstimateAdjuster(var cap: Double) : AdaptiveFuelBurnEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveFuelBurnEstimate, value: Double): Double = minOf(value, cap)
}

data class AdaptiveFuelBurnEstimateRatioOffsetAdjuster(var weight: Double) : AdaptiveFuelBurnEstimateModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: AdaptiveFuelBurnEstimate, value: Double): Double = value + item.ratio * weight
}

/** Runs the adaptive fuel burn estimate steps in order and keeps a trace of each. */
data class AdaptiveFuelBurnEstimateEngine(
    var steps: MutableList<AdaptiveFuelBurnEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveFuelBurnEstimate): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveFuelBurnEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveFuelBurnEstimateEngine(): AdaptiveFuelBurnEstimateEngine {
    return AdaptiveFuelBurnEstimateEngine(
        steps = mutableListOf<AdaptiveFuelBurnEstimateModifier>(
            PercentAdaptiveFuelBurnEstimateAdjuster(-3.5),
            SourceAdaptiveFuelBurnEstimateAdjuster(
                mutableMapOf<String, Double>("central" to 1.03, "online" to 0.72, "west" to 1.06),
            ),
            CapAdaptiveFuelBurnEstimateAdjuster(3935.0),
            AdaptiveFuelBurnEstimateRatioOffsetAdjuster(0.61),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 88757.7,
    )
}
