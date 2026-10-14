package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered proof of delivery rollup calculation. */
interface TieredProofOfDeliveryRollupAdjuster {
    fun describe(): String
    fun adjust(item: TieredProofOfDeliveryRollup, value: Double): Double
}

data class FlaggedTieredProofOfDeliveryRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredProofOfDeliveryRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredProofOfDeliveryRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTieredProofOfDeliveryRollupAdjuster(
    var percent: Double,
) : TieredProofOfDeliveryRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredProofOfDeliveryRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredProofOfDeliveryRollupAdjuster(var step: Double) : TieredProofOfDeliveryRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredProofOfDeliveryRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered proof of delivery rollup steps in order and keeps a trace of each. */
data class TieredProofOfDeliveryRollupEngine(
    var steps: MutableList<TieredProofOfDeliveryRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredProofOfDeliveryRollup): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredProofOfDeliveryRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredProofOfDeliveryRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredProofOfDeliveryRollupEngine(flags: Registry): TieredProofOfDeliveryRollupEngine {
    return TieredProofOfDeliveryRollupEngine(
        steps = mutableListOf<TieredProofOfDeliveryRollupAdjuster>(
            FlaggedTieredProofOfDeliveryRollupAdjuster(flags, "tiered-proof-of-delivery-rollup-adaptive", -3.8),
            PercentTieredProofOfDeliveryRollupAdjuster(0.1),
            RoundingTieredProofOfDeliveryRollupAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 50892.0,
    )
}
