package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.flags.Registry

/** One step of the weighted backhaul target calculation. */
interface WeightedBackhaulTargetModifier {
    fun describe(): String
    fun adjust(item: WeightedBackhaulTarget, value: Double): Double
}

data class WeightedBackhaulTargetQuantityOffsetAdjuster(var weight: Double) : WeightedBackhaulTargetModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: WeightedBackhaulTarget, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class TieredWeightedBackhaulTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedBackhaulTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedBackhaulTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedBackhaulTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedBackhaulTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedBackhaulTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted backhaul target steps in order and keeps a trace of each. */
data class WeightedBackhaulTargetEngine(
    var steps: MutableList<WeightedBackhaulTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedBackhaulTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedBackhaulTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedBackhaulTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedBackhaulTargetEngine(flags: Registry): WeightedBackhaulTargetEngine {
    return WeightedBackhaulTargetEngine(
        steps = mutableListOf<WeightedBackhaulTargetModifier>(
            WeightedBackhaulTargetQuantityOffsetAdjuster(0.38),
            TieredWeightedBackhaulTargetAdjuster(
                mutableListOf<Double>(2446.0, 4534.0, 6580.0, 6785.0),
                mutableListOf<Double>(0.82, 0.96, 0.85, 1.18),
            ),
            FlaggedWeightedBackhaulTargetAdjuster(flags, "weighted-backhaul-target-tiered", 4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 68780.3,
    )
}
