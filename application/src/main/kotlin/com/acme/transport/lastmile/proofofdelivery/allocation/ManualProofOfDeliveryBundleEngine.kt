package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual proof of delivery bundle calculation. */
interface ManualProofOfDeliveryBundleModifier {
    fun describe(): String
    fun adjust(item: ManualProofOfDeliveryBundle, value: Double): Double
}

data class MinimumManualProofOfDeliveryBundleAdjuster(
    var minimum: Double,
) : ManualProofOfDeliveryBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualProofOfDeliveryBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelManualProofOfDeliveryBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualProofOfDeliveryBundleModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualProofOfDeliveryBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualProofOfDeliveryBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualProofOfDeliveryBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualProofOfDeliveryBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapManualProofOfDeliveryBundleAdjuster(var cap: Double) : ManualProofOfDeliveryBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualProofOfDeliveryBundle, value: Double): Double = minOf(value, cap)
}

data class RoundingManualProofOfDeliveryBundleAdjuster(var step: Double) : ManualProofOfDeliveryBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualProofOfDeliveryBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the manual proof of delivery bundle steps in order and keeps a trace of each. */
data class ManualProofOfDeliveryBundleEngine(
    var steps: MutableList<ManualProofOfDeliveryBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualProofOfDeliveryBundle): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualProofOfDeliveryBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualProofOfDeliveryBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualProofOfDeliveryBundleEngine(): ManualProofOfDeliveryBundleEngine {
    return ManualProofOfDeliveryBundleEngine(
        steps = mutableListOf<ManualProofOfDeliveryBundleModifier>(
            MinimumManualProofOfDeliveryBundleAdjuster(9.9),
            LabelManualProofOfDeliveryBundleAdjuster(
                mutableMapOf<String, Double>("east" to 0.9, "west" to 0.73, "central" to 0.75),
            ),
            TieredManualProofOfDeliveryBundleAdjuster(
                mutableListOf<Double>(100.0, 4227.0),
                mutableListOf<Double>(0.9, 1.16),
            ),
            CapManualProofOfDeliveryBundleAdjuster(12071.0),
            RoundingManualProofOfDeliveryBundleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 1356.0,
    )
}
