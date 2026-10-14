package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary proof of delivery reservation calculation. */
interface PrimaryProofOfDeliveryReservationFactor {
    fun describe(): String
    fun adjust(item: PrimaryProofOfDeliveryReservation, value: Double): Double
}

data class CapPrimaryProofOfDeliveryReservationAdjuster(
    var cap: Double,
) : PrimaryProofOfDeliveryReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryProofOfDeliveryReservation, value: Double): Double = minOf(value, cap)
}

data class PercentPrimaryProofOfDeliveryReservationAdjuster(
    var percent: Double,
) : PrimaryProofOfDeliveryReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryProofOfDeliveryReservation, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryProofOfDeliveryReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryProofOfDeliveryReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryProofOfDeliveryReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryProofOfDeliveryReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryProofOfDeliveryReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryProofOfDeliveryReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryProofOfDeliveryReservationAdjuster(
    var minimum: Double,
) : PrimaryProofOfDeliveryReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryProofOfDeliveryReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryPrimaryProofOfDeliveryReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryProofOfDeliveryReservationFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryProofOfDeliveryReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary proof of delivery reservation steps in order and keeps a trace of each. */
data class PrimaryProofOfDeliveryReservationEngine(
    var steps: MutableList<PrimaryProofOfDeliveryReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryProofOfDeliveryReservation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryProofOfDeliveryReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryProofOfDeliveryReservationEngine(flags: Registry): PrimaryProofOfDeliveryReservationEngine {
    return PrimaryProofOfDeliveryReservationEngine(
        steps = mutableListOf<PrimaryProofOfDeliveryReservationFactor>(
            CapPrimaryProofOfDeliveryReservationAdjuster(37773.0),
            PercentPrimaryProofOfDeliveryReservationAdjuster(9.4),
            TieredPrimaryProofOfDeliveryReservationAdjuster(
                mutableListOf<Double>(1714.0, 3238.0, 6579.0),
                mutableListOf<Double>(1.18, 1.18, 1.16),
            ),
            FlaggedPrimaryProofOfDeliveryReservationAdjuster(
                flags,
                "primary-proof-of-delivery-reservation-manual",
                1.2,
            ),
            MinimumPrimaryProofOfDeliveryReservationAdjuster(40.2),
            CountryPrimaryProofOfDeliveryReservationAdjuster(
                mutableMapOf<String, Double>("north" to 0.82, "south" to 1.17, "east" to 0.75),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 86359.3,
    )
}
