package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted swap record calculation. */
interface WeightedSwapRecordStep {
    fun describe(): String
    fun adjust(item: WeightedSwapRecord, value: Double): Double
}

data class TieredWeightedSwapRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedSwapRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedSwapRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryWeightedSwapRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedSwapRecordStep {
    override fun describe(): String = "by category"

    override fun adjust(item: WeightedSwapRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedSwapRecordAdjuster(var step: Double) : WeightedSwapRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedSwapRecord, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedSwapRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedSwapRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedSwapRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedSwapRecordAdjuster(var percent: Double) : WeightedSwapRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedSwapRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted swap record steps in order and keeps a trace of each. */
data class WeightedSwapRecordEngine(
    var steps: MutableList<WeightedSwapRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedSwapRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedSwapRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedSwapRecordEngine(flags: Registry): WeightedSwapRecordEngine {
    return WeightedSwapRecordEngine(
        steps = mutableListOf<WeightedSwapRecordStep>(
            TieredWeightedSwapRecordAdjuster(
                mutableListOf<Double>(1037.0, 2300.0, 3140.0, 7962.0),
                mutableListOf<Double>(1.2, 0.99, 1.05, 1.18),
            ),
            CategoryWeightedSwapRecordAdjuster(mutableMapOf<String, Double>("west" to 0.82, "retail" to 1.22)),
            RoundingWeightedSwapRecordAdjuster(0.1),
            FlaggedWeightedSwapRecordAdjuster(flags, "weighted-swap-record-weighted", 4.8),
            PercentWeightedSwapRecordAdjuster(19.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 2089.9,
    )
}
