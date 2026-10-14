package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted ev charge quota calculation. */
interface WeightedEvChargeQuotaModifier {
    fun describe(): String
    fun adjust(item: WeightedEvChargeQuota, value: Double): Double
}

data class WeightedEvChargeQuotaPalletsOffsetAdjuster(var weight: Double) : WeightedEvChargeQuotaModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: WeightedEvChargeQuota, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class FlaggedWeightedEvChargeQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedEvChargeQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedEvChargeQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumWeightedEvChargeQuotaAdjuster(var minimum: Double) : WeightedEvChargeQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedEvChargeQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedEvChargeQuotaAdjuster(var step: Double) : WeightedEvChargeQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedEvChargeQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted ev charge quota steps in order and keeps a trace of each. */
data class WeightedEvChargeQuotaEngine(
    var steps: MutableList<WeightedEvChargeQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedEvChargeQuota): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedEvChargeQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedEvChargeQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedEvChargeQuotaEngine(flags: Registry): WeightedEvChargeQuotaEngine {
    return WeightedEvChargeQuotaEngine(
        steps = mutableListOf<WeightedEvChargeQuotaModifier>(
            WeightedEvChargeQuotaPalletsOffsetAdjuster(0.28),
            FlaggedWeightedEvChargeQuotaAdjuster(flags, "weighted-ev-charge-quota-default", -7.4),
            MinimumWeightedEvChargeQuotaAdjuster(23.6),
            RoundingWeightedEvChargeQuotaAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 67143.8,
    )
}
