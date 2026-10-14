package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.flags.Registry

/** One step of the proof of delivery rollup calculation. */
interface ProofOfDeliveryRollupFactor {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryRollup, value: Double): Double
}

data class FlaggedProofOfDeliveryRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredProofOfDeliveryRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentProofOfDeliveryRollupAdjuster(var percent: Double) : ProofOfDeliveryRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumProofOfDeliveryRollupAdjuster(var minimum: Double) : ProofOfDeliveryRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ProofOfDeliveryRollupAttemptsOffsetAdjuster(var weight: Double) : ProofOfDeliveryRollupFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ProofOfDeliveryRollup, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the proof of delivery rollup steps in order and keeps a trace of each. */
data class ProofOfDeliveryRollupEngine(
    var steps: MutableList<ProofOfDeliveryRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryRollup): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryRollupEngine(flags: Registry): ProofOfDeliveryRollupEngine {
    return ProofOfDeliveryRollupEngine(
        steps = mutableListOf<ProofOfDeliveryRollupFactor>(
            FlaggedProofOfDeliveryRollupAdjuster(flags, "proof-of-delivery-rollup-tiered", 0.0),
            TieredProofOfDeliveryRollupAdjuster(
                mutableListOf<Double>(470.0, 2741.0, 8056.0, 8435.0),
                mutableListOf<Double>(0.87, 0.91, 0.94, 0.87),
            ),
            PercentProofOfDeliveryRollupAdjuster(-9.2),
            MinimumProofOfDeliveryRollupAdjuster(39.5),
            ProofOfDeliveryRollupAttemptsOffsetAdjuster(1.11),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 40594.7,
    )
}
