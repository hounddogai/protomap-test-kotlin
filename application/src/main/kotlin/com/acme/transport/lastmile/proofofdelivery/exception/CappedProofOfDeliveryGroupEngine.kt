package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped proof of delivery group calculation. */
interface CappedProofOfDeliveryGroupFactor {
    fun describe(): String
    fun adjust(item: CappedProofOfDeliveryGroup, value: Double): Double
}

data class PercentCappedProofOfDeliveryGroupAdjuster(var percent: Double) : CappedProofOfDeliveryGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedProofOfDeliveryGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedProofOfDeliveryGroupWindowDaysOffsetAdjuster(
    var weight: Double,
) : CappedProofOfDeliveryGroupFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: CappedProofOfDeliveryGroup, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class TieredCappedProofOfDeliveryGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedProofOfDeliveryGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedProofOfDeliveryGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped proof of delivery group steps in order and keeps a trace of each. */
data class CappedProofOfDeliveryGroupEngine(
    var steps: MutableList<CappedProofOfDeliveryGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedProofOfDeliveryGroup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedProofOfDeliveryGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedProofOfDeliveryGroupEngine(): CappedProofOfDeliveryGroupEngine {
    return CappedProofOfDeliveryGroupEngine(
        steps = mutableListOf<CappedProofOfDeliveryGroupFactor>(
            PercentCappedProofOfDeliveryGroupAdjuster(13.6),
            CappedProofOfDeliveryGroupWindowDaysOffsetAdjuster(1.29),
            TieredCappedProofOfDeliveryGroupAdjuster(
                mutableListOf<Double>(31.0, 3877.0, 4845.0, 7691.0),
                mutableListOf<Double>(1.17, 0.98, 0.96, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 32999.3,
    )
}
