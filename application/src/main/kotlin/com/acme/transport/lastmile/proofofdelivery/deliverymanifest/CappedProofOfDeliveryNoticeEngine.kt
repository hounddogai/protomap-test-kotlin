package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped proof of delivery notice calculation. */
interface CappedProofOfDeliveryNoticeModifier {
    fun describe(): String
    fun adjust(item: CappedProofOfDeliveryNotice, value: Double): Double
}

data class FlaggedCappedProofOfDeliveryNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedProofOfDeliveryNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedProofOfDeliveryNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCappedProofOfDeliveryNoticeAdjuster(
    var minimum: Double,
) : CappedProofOfDeliveryNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedProofOfDeliveryNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedProofOfDeliveryNoticeAdjuster(var step: Double) : CappedProofOfDeliveryNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedProofOfDeliveryNotice, value: Double): Double = roundTo(value, step)
}

data class CappedProofOfDeliveryNoticeAmountOffsetAdjuster(
    var weight: Double,
) : CappedProofOfDeliveryNoticeModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CappedProofOfDeliveryNotice, value: Double): Double = value + item.amount * weight
}

/** Runs the capped proof of delivery notice steps in order and keeps a trace of each. */
data class CappedProofOfDeliveryNoticeEngine(
    var steps: MutableList<CappedProofOfDeliveryNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedProofOfDeliveryNotice): Double {
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

    fun evaluateAll(items: MutableList<CappedProofOfDeliveryNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedProofOfDeliveryNoticeEngine(flags: Registry): CappedProofOfDeliveryNoticeEngine {
    return CappedProofOfDeliveryNoticeEngine(
        steps = mutableListOf<CappedProofOfDeliveryNoticeModifier>(
            FlaggedCappedProofOfDeliveryNoticeAdjuster(flags, "capped-proof-of-delivery-notice-legacy", -0.5),
            MinimumCappedProofOfDeliveryNoticeAdjuster(46.5),
            RoundingCappedProofOfDeliveryNoticeAdjuster(0.1),
            CappedProofOfDeliveryNoticeAmountOffsetAdjuster(1.55),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 78768.5,
    )
}
