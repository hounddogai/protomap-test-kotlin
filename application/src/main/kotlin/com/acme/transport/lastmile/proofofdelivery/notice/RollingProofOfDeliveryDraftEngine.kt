package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling proof of delivery draft calculation. */
interface RollingProofOfDeliveryDraftAdjuster {
    fun describe(): String
    fun adjust(item: RollingProofOfDeliveryDraft, value: Double): Double
}

data class RoundingRollingProofOfDeliveryDraftAdjuster(var step: Double) : RollingProofOfDeliveryDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingProofOfDeliveryDraft, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingProofOfDeliveryDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingProofOfDeliveryDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingProofOfDeliveryDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingProofOfDeliveryDraftAdjuster(var cap: Double) : RollingProofOfDeliveryDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingProofOfDeliveryDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingProofOfDeliveryDraftAdjuster(
    var minimum: Double,
) : RollingProofOfDeliveryDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingProofOfDeliveryDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingProofOfDeliveryDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingProofOfDeliveryDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingProofOfDeliveryDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingProofOfDeliveryDraftUpdatedAtOffsetAdjuster(
    var weight: Double,
) : RollingProofOfDeliveryDraftAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: RollingProofOfDeliveryDraft, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

/** Runs the rolling proof of delivery draft steps in order and keeps a trace of each. */
data class RollingProofOfDeliveryDraftEngine(
    var steps: MutableList<RollingProofOfDeliveryDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingProofOfDeliveryDraft): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingProofOfDeliveryDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingProofOfDeliveryDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingProofOfDeliveryDraftEngine(flags: Registry): RollingProofOfDeliveryDraftEngine {
    return RollingProofOfDeliveryDraftEngine(
        steps = mutableListOf<RollingProofOfDeliveryDraftAdjuster>(
            RoundingRollingProofOfDeliveryDraftAdjuster(5.0),
            FlaggedRollingProofOfDeliveryDraftAdjuster(flags, "rolling-proof-of-delivery-draft-adaptive", -7.3),
            CapRollingProofOfDeliveryDraftAdjuster(15144.0),
            MinimumRollingProofOfDeliveryDraftAdjuster(34.2),
            TieredRollingProofOfDeliveryDraftAdjuster(
                mutableListOf<Double>(946.0, 4764.0, 5000.0, 6095.0),
                mutableListOf<Double>(0.8, 1.07, 0.99, 1.1),
            ),
            RollingProofOfDeliveryDraftUpdatedAtOffsetAdjuster(0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 60887.3,
    )
}
