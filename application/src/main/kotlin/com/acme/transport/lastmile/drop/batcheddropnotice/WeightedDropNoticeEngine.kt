package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted drop notice calculation. */
interface WeightedDropNoticeStep {
    fun describe(): String
    fun adjust(item: WeightedDropNotice, value: Double): Double
}

data class RoundingWeightedDropNoticeAdjuster(var step: Double) : WeightedDropNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDropNotice, value: Double): Double = roundTo(value, step)
}

data class TieredWeightedDropNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDropNoticeStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDropNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionWeightedDropNoticeAdjuster(var factors: MutableMap<String, Double>) : WeightedDropNoticeStep {
    override fun describe(): String = "by region"

    override fun adjust(item: WeightedDropNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted drop notice steps in order and keeps a trace of each. */
data class WeightedDropNoticeEngine(
    var steps: MutableList<WeightedDropNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedDropNotice): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedDropNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedDropNoticeEngine(): WeightedDropNoticeEngine {
    return WeightedDropNoticeEngine(
        steps = mutableListOf<WeightedDropNoticeStep>(
            RoundingWeightedDropNoticeAdjuster(1.0),
            TieredWeightedDropNoticeAdjuster(
                mutableListOf<Double>(4963.0, 9474.0),
                mutableListOf<Double>(1.12, 1.05),
            ),
            RegionWeightedDropNoticeAdjuster(mutableMapOf<String, Double>("central" to 1.21, "retail" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 44074.4,
    )
}
