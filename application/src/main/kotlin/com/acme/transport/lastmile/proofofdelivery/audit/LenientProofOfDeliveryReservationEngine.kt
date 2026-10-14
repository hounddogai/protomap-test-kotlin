package com.acme.transport.lastmile.proofofdelivery.audit

/** One step of the lenient proof of delivery reservation calculation. */
interface LenientProofOfDeliveryReservationStep {
    fun describe(): String
    fun adjust(item: LenientProofOfDeliveryReservation, value: Double): Double
}

data class CapLenientProofOfDeliveryReservationAdjuster(
    var cap: Double,
) : LenientProofOfDeliveryReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientProofOfDeliveryReservation, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientProofOfDeliveryReservationAdjuster(
    var minimum: Double,
) : LenientProofOfDeliveryReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientProofOfDeliveryReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionLenientProofOfDeliveryReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientProofOfDeliveryReservationStep {
    override fun describe(): String = "by region"

    override fun adjust(item: LenientProofOfDeliveryReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient proof of delivery reservation steps in order and keeps a trace of each. */
data class LenientProofOfDeliveryReservationEngine(
    var steps: MutableList<LenientProofOfDeliveryReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientProofOfDeliveryReservation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientProofOfDeliveryReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientProofOfDeliveryReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientProofOfDeliveryReservationEngine(): LenientProofOfDeliveryReservationEngine {
    return LenientProofOfDeliveryReservationEngine(
        steps = mutableListOf<LenientProofOfDeliveryReservationStep>(
            CapLenientProofOfDeliveryReservationAdjuster(40059.0),
            MinimumLenientProofOfDeliveryReservationAdjuster(16.3),
            RegionLenientProofOfDeliveryReservationAdjuster(
                mutableMapOf<String, Double>("west" to 0.81, "east" to 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 32030.1,
    )
}
