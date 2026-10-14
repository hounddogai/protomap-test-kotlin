package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary broker reservation calculation. */
interface PrimaryBrokerReservationModifier {
    fun describe(): String
    fun adjust(item: PrimaryBrokerReservation, value: Double): Double
}

data class LabelPrimaryBrokerReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryBrokerReservationModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryBrokerReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryBrokerReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryBrokerReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryBrokerReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryBrokerReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryBrokerReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryBrokerReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryBrokerReservationAdjuster(var step: Double) : PrimaryBrokerReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryBrokerReservation, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryBrokerReservationAdjuster(var cap: Double) : PrimaryBrokerReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryBrokerReservation, value: Double): Double = minOf(value, cap)
}

data class PrimaryBrokerReservationDistanceKmOffsetAdjuster(
    var weight: Double,
) : PrimaryBrokerReservationModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: PrimaryBrokerReservation, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the primary broker reservation steps in order and keeps a trace of each. */
data class PrimaryBrokerReservationEngine(
    var steps: MutableList<PrimaryBrokerReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryBrokerReservation): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryBrokerReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryBrokerReservationEngine(flags: Registry): PrimaryBrokerReservationEngine {
    return PrimaryBrokerReservationEngine(
        steps = mutableListOf<PrimaryBrokerReservationModifier>(
            LabelPrimaryBrokerReservationAdjuster(mutableMapOf<String, Double>("north" to 0.81)),
            FlaggedPrimaryBrokerReservationAdjuster(flags, "primary-broker-reservation-batched", -9.8),
            TieredPrimaryBrokerReservationAdjuster(
                mutableListOf<Double>(1031.0, 9604.0),
                mutableListOf<Double>(0.96, 1.16),
            ),
            RoundingPrimaryBrokerReservationAdjuster(0.05),
            CapPrimaryBrokerReservationAdjuster(32803.0),
            PrimaryBrokerReservationDistanceKmOffsetAdjuster(1.66),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 59934.5,
    )
}
