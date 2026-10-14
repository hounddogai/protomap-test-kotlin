package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy proof of delivery request calculation. */
interface LegacyProofOfDeliveryRequestFactor {
    fun describe(): String
    fun adjust(item: LegacyProofOfDeliveryRequest, value: Double): Double
}

data class RoundingLegacyProofOfDeliveryRequestAdjuster(var step: Double) : LegacyProofOfDeliveryRequestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyProofOfDeliveryRequest, value: Double): Double = roundTo(value, step)
}

data class CapLegacyProofOfDeliveryRequestAdjuster(var cap: Double) : LegacyProofOfDeliveryRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyProofOfDeliveryRequest, value: Double): Double = minOf(value, cap)
}

data class LegacyProofOfDeliveryRequestCreatedAtOffsetAdjuster(
    var weight: Double,
) : LegacyProofOfDeliveryRequestFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: LegacyProofOfDeliveryRequest, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class MinimumLegacyProofOfDeliveryRequestAdjuster(
    var minimum: Double,
) : LegacyProofOfDeliveryRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyProofOfDeliveryRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleLegacyProofOfDeliveryRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyProofOfDeliveryRequestFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyProofOfDeliveryRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyProofOfDeliveryRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyProofOfDeliveryRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyProofOfDeliveryRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy proof of delivery request steps in order and keeps a trace of each. */
data class LegacyProofOfDeliveryRequestEngine(
    var steps: MutableList<LegacyProofOfDeliveryRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyProofOfDeliveryRequest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyProofOfDeliveryRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyProofOfDeliveryRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyProofOfDeliveryRequestEngine(flags: Registry): LegacyProofOfDeliveryRequestEngine {
    return LegacyProofOfDeliveryRequestEngine(
        steps = mutableListOf<LegacyProofOfDeliveryRequestFactor>(
            RoundingLegacyProofOfDeliveryRequestAdjuster(5.0),
            CapLegacyProofOfDeliveryRequestAdjuster(42541.0),
            LegacyProofOfDeliveryRequestCreatedAtOffsetAdjuster(0.58),
            MinimumLegacyProofOfDeliveryRequestAdjuster(42.7),
            TitleLegacyProofOfDeliveryRequestAdjuster(
                mutableMapOf<String, Double>("west" to 1.08, "south" to 1.21),
            ),
            FlaggedLegacyProofOfDeliveryRequestAdjuster(flags, "legacy-proof-of-delivery-request-batched", -7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 11249.2,
    )
}
