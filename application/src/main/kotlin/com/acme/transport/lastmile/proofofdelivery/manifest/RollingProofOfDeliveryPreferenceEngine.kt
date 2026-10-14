package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling proof of delivery preference calculation. */
interface RollingProofOfDeliveryPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: RollingProofOfDeliveryPreference, value: Double): Double
}

data class FlaggedRollingProofOfDeliveryPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingProofOfDeliveryPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingProofOfDeliveryPreferenceAdjuster(
    var cap: Double,
) : RollingProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingProofOfDeliveryPreference, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingProofOfDeliveryPreferenceAdjuster(
    var step: Double,
) : RollingProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingProofOfDeliveryPreference, value: Double): Double = roundTo(value, step)
}

data class TieredRollingProofOfDeliveryPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingProofOfDeliveryPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingProofOfDeliveryPreferenceWeightOffsetAdjuster(
    var weight: Double,
) : RollingProofOfDeliveryPreferenceAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: RollingProofOfDeliveryPreference, value: Double): Double = value + item.weight * weight
}

/** Runs the rolling proof of delivery preference steps in order and keeps a trace of each. */
data class RollingProofOfDeliveryPreferenceEngine(
    var steps: MutableList<RollingProofOfDeliveryPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingProofOfDeliveryPreference): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingProofOfDeliveryPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingProofOfDeliveryPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingProofOfDeliveryPreferenceEngine(flags: Registry): RollingProofOfDeliveryPreferenceEngine {
    return RollingProofOfDeliveryPreferenceEngine(
        steps = mutableListOf<RollingProofOfDeliveryPreferenceAdjuster>(
            FlaggedRollingProofOfDeliveryPreferenceAdjuster(
                flags,
                "rolling-proof-of-delivery-preference-pending",
                -6.3,
            ),
            CapRollingProofOfDeliveryPreferenceAdjuster(40543.0),
            RoundingRollingProofOfDeliveryPreferenceAdjuster(0.05),
            TieredRollingProofOfDeliveryPreferenceAdjuster(
                mutableListOf<Double>(1263.0, 3124.0, 4620.0, 7932.0),
                mutableListOf<Double>(1.18, 0.85, 1.06, 0.85),
            ),
            RollingProofOfDeliveryPreferenceWeightOffsetAdjuster(0.72),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 31472.2,
    )
}
