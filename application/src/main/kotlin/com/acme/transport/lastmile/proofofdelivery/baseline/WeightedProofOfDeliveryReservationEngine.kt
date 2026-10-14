package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted proof of delivery reservation calculation. */
interface WeightedProofOfDeliveryReservationFactor {
    fun describe(): String
    fun adjust(item: WeightedProofOfDeliveryReservation, value: Double): Double
}

data class PercentWeightedProofOfDeliveryReservationAdjuster(
    var percent: Double,
) : WeightedProofOfDeliveryReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedProofOfDeliveryReservation, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneWeightedProofOfDeliveryReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedProofOfDeliveryReservationFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: WeightedProofOfDeliveryReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedProofOfDeliveryReservationAdjuster(
    var step: Double,
) : WeightedProofOfDeliveryReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedProofOfDeliveryReservation, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedProofOfDeliveryReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedProofOfDeliveryReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedProofOfDeliveryReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted proof of delivery reservation steps in order and keeps a trace of each. */
data class WeightedProofOfDeliveryReservationEngine(
    var steps: MutableList<WeightedProofOfDeliveryReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedProofOfDeliveryReservation): Double {
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

    fun evaluateAll(items: MutableList<WeightedProofOfDeliveryReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedProofOfDeliveryReservationEngine(flags: Registry): WeightedProofOfDeliveryReservationEngine {
    return WeightedProofOfDeliveryReservationEngine(
        steps = mutableListOf<WeightedProofOfDeliveryReservationFactor>(
            PercentWeightedProofOfDeliveryReservationAdjuster(-17.5),
            LaneWeightedProofOfDeliveryReservationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.17, "online" to 0.76, "central" to 0.75),
            ),
            RoundingWeightedProofOfDeliveryReservationAdjuster(5.0),
            FlaggedWeightedProofOfDeliveryReservationAdjuster(
                flags,
                "weighted-proof-of-delivery-reservation-tiered",
                -5.5,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 41542.2,
    )
}
