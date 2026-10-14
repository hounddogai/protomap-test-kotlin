package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling proof of delivery entry calculation. */
interface RollingProofOfDeliveryEntryModifier {
    fun describe(): String
    fun adjust(item: RollingProofOfDeliveryEntry, value: Double): Double
}

data class CapRollingProofOfDeliveryEntryAdjuster(var cap: Double) : RollingProofOfDeliveryEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingProofOfDeliveryEntry, value: Double): Double = minOf(value, cap)
}

data class PercentRollingProofOfDeliveryEntryAdjuster(
    var percent: Double,
) : RollingProofOfDeliveryEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingProofOfDeliveryEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingProofOfDeliveryEntryAdjuster(var step: Double) : RollingProofOfDeliveryEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingProofOfDeliveryEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling proof of delivery entry steps in order and keeps a trace of each. */
data class RollingProofOfDeliveryEntryEngine(
    var steps: MutableList<RollingProofOfDeliveryEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingProofOfDeliveryEntry): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingProofOfDeliveryEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingProofOfDeliveryEntryEngine(): RollingProofOfDeliveryEntryEngine {
    return RollingProofOfDeliveryEntryEngine(
        steps = mutableListOf<RollingProofOfDeliveryEntryModifier>(
            CapRollingProofOfDeliveryEntryAdjuster(40804.0),
            PercentRollingProofOfDeliveryEntryAdjuster(-1.3),
            RoundingRollingProofOfDeliveryEntryAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 1143.6,
    )
}
