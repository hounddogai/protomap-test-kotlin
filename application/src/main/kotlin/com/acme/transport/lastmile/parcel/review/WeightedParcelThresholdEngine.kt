package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted parcel threshold calculation. */
interface WeightedParcelThresholdStep {
    fun describe(): String
    fun adjust(item: WeightedParcelThreshold, value: Double): Double
}

data class TitleWeightedParcelThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedParcelThresholdStep {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedParcelThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedParcelThresholdPalletsOffsetAdjuster(var weight: Double) : WeightedParcelThresholdStep {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: WeightedParcelThreshold, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class PercentWeightedParcelThresholdAdjuster(var percent: Double) : WeightedParcelThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedParcelThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted parcel threshold steps in order and keeps a trace of each. */
data class WeightedParcelThresholdEngine(
    var steps: MutableList<WeightedParcelThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedParcelThreshold): Double {
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

    fun evaluateAll(items: MutableList<WeightedParcelThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedParcelThresholdEngine(): WeightedParcelThresholdEngine {
    return WeightedParcelThresholdEngine(
        steps = mutableListOf<WeightedParcelThresholdStep>(
            TitleWeightedParcelThresholdAdjuster(
                mutableMapOf<String, Double>("south" to 0.97, "retail" to 1.31),
            ),
            WeightedParcelThresholdPalletsOffsetAdjuster(1.6),
            PercentWeightedParcelThresholdAdjuster(22.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 28002.7,
    )
}
