package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy proof of delivery window calculation. */
interface LegacyProofOfDeliveryWindowFactor {
    fun describe(): String
    fun adjust(item: LegacyProofOfDeliveryWindow, value: Double): Double
}

data class CapLegacyProofOfDeliveryWindowAdjuster(var cap: Double) : LegacyProofOfDeliveryWindowFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyProofOfDeliveryWindow, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyProofOfDeliveryWindowAdjuster(var step: Double) : LegacyProofOfDeliveryWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyProofOfDeliveryWindow, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacyProofOfDeliveryWindowAdjuster(var minimum: Double) : LegacyProofOfDeliveryWindowFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyProofOfDeliveryWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy proof of delivery window steps in order and keeps a trace of each. */
data class LegacyProofOfDeliveryWindowEngine(
    var steps: MutableList<LegacyProofOfDeliveryWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyProofOfDeliveryWindow): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyProofOfDeliveryWindowFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyProofOfDeliveryWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyProofOfDeliveryWindowEngine(): LegacyProofOfDeliveryWindowEngine {
    return LegacyProofOfDeliveryWindowEngine(
        steps = mutableListOf<LegacyProofOfDeliveryWindowFactor>(
            CapLegacyProofOfDeliveryWindowAdjuster(15254.0),
            RoundingLegacyProofOfDeliveryWindowAdjuster(5.0),
            MinimumLegacyProofOfDeliveryWindowAdjuster(44.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 36067.5,
    )
}
