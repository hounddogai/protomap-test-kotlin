package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling proof of delivery quota calculation. */
interface RollingProofOfDeliveryQuotaModifier {
    fun describe(): String
    fun adjust(item: RollingProofOfDeliveryQuota, value: Double): Double
}

data class CapRollingProofOfDeliveryQuotaAdjuster(var cap: Double) : RollingProofOfDeliveryQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingProofOfDeliveryQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingProofOfDeliveryQuotaAdjuster(
    var minimum: Double,
) : RollingProofOfDeliveryQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingProofOfDeliveryQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRollingProofOfDeliveryQuotaAdjuster(var step: Double) : RollingProofOfDeliveryQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingProofOfDeliveryQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling proof of delivery quota steps in order and keeps a trace of each. */
data class RollingProofOfDeliveryQuotaEngine(
    var steps: MutableList<RollingProofOfDeliveryQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingProofOfDeliveryQuota): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingProofOfDeliveryQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingProofOfDeliveryQuotaEngine(): RollingProofOfDeliveryQuotaEngine {
    return RollingProofOfDeliveryQuotaEngine(
        steps = mutableListOf<RollingProofOfDeliveryQuotaModifier>(
            CapRollingProofOfDeliveryQuotaAdjuster(38888.0),
            MinimumRollingProofOfDeliveryQuotaAdjuster(11.0),
            RoundingRollingProofOfDeliveryQuotaAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 58113.0,
    )
}
