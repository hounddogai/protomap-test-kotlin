package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted capacity preference calculation. */
interface WeightedCapacityPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: WeightedCapacityPreference, value: Double): Double
}

data class SourceWeightedCapacityPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCapacityPreferenceAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: WeightedCapacityPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedCapacityPreferenceBudgetOffsetAdjuster(
    var weight: Double,
) : WeightedCapacityPreferenceAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: WeightedCapacityPreference, value: Double): Double = value + item.budget * weight
}

data class FlaggedWeightedCapacityPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCapacityPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCapacityPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingWeightedCapacityPreferenceAdjuster(var step: Double) : WeightedCapacityPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedCapacityPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted capacity preference steps in order and keeps a trace of each. */
data class WeightedCapacityPreferenceEngine(
    var steps: MutableList<WeightedCapacityPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedCapacityPreference): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedCapacityPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedCapacityPreferenceEngine(flags: Registry): WeightedCapacityPreferenceEngine {
    return WeightedCapacityPreferenceEngine(
        steps = mutableListOf<WeightedCapacityPreferenceAdjuster>(
            SourceWeightedCapacityPreferenceAdjuster(mutableMapOf<String, Double>("online" to 0.77)),
            WeightedCapacityPreferenceBudgetOffsetAdjuster(0.55),
            FlaggedWeightedCapacityPreferenceAdjuster(flags, "weighted-capacity-preference-tiered", -7.7),
            RoundingWeightedCapacityPreferenceAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 8272.3,
    )
}
