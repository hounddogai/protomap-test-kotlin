package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.flags.Registry

/** One step of the primary proof of delivery notice calculation. */
interface PrimaryProofOfDeliveryNoticeAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryProofOfDeliveryNotice, value: Double): Double
}

data class FlaggedPrimaryProofOfDeliveryNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryProofOfDeliveryNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryProofOfDeliveryNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryProofOfDeliveryNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryProofOfDeliveryNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryProofOfDeliveryNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryProofOfDeliveryNoticeAdjuster(
    var minimum: Double,
) : PrimaryProofOfDeliveryNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryProofOfDeliveryNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary proof of delivery notice steps in order and keeps a trace of each. */
data class PrimaryProofOfDeliveryNoticeEngine(
    var steps: MutableList<PrimaryProofOfDeliveryNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryProofOfDeliveryNotice): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryProofOfDeliveryNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryProofOfDeliveryNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryProofOfDeliveryNoticeEngine(flags: Registry): PrimaryProofOfDeliveryNoticeEngine {
    return PrimaryProofOfDeliveryNoticeEngine(
        steps = mutableListOf<PrimaryProofOfDeliveryNoticeAdjuster>(
            FlaggedPrimaryProofOfDeliveryNoticeAdjuster(flags, "primary-proof-of-delivery-notice-strict", -3.2),
            TieredPrimaryProofOfDeliveryNoticeAdjuster(
                mutableListOf<Double>(7498.0, 8818.0),
                mutableListOf<Double>(1.09, 0.93),
            ),
            MinimumPrimaryProofOfDeliveryNoticeAdjuster(36.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 81208.0,
    )
}
