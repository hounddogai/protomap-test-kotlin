package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered capacity reservation calculation. */
interface TieredCapacityReservationFactor {
    fun describe(): String
    fun adjust(item: TieredCapacityReservation, value: Double): Double
}

data class TieredTieredCapacityReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCapacityReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCapacityReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTieredCapacityReservationAdjuster(var minimum: Double) : TieredCapacityReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCapacityReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredCapacityReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCapacityReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCapacityReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredCapacityReservationAdjuster(var cap: Double) : TieredCapacityReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredCapacityReservation, value: Double): Double = minOf(value, cap)
}

data class TieredCapacityReservationThresholdOffsetAdjuster(
    var weight: Double,
) : TieredCapacityReservationFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: TieredCapacityReservation, value: Double): Double = value + item.threshold * weight
}

data class PercentTieredCapacityReservationAdjuster(var percent: Double) : TieredCapacityReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCapacityReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered capacity reservation steps in order and keeps a trace of each. */
data class TieredCapacityReservationEngine(
    var steps: MutableList<TieredCapacityReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredCapacityReservation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredCapacityReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredCapacityReservationEngine(flags: Registry): TieredCapacityReservationEngine {
    return TieredCapacityReservationEngine(
        steps = mutableListOf<TieredCapacityReservationFactor>(
            TieredTieredCapacityReservationAdjuster(
                mutableListOf<Double>(2253.0, 2877.0, 4191.0, 6794.0),
                mutableListOf<Double>(0.84, 0.96, 1.18, 1.12),
            ),
            MinimumTieredCapacityReservationAdjuster(39.7),
            FlaggedTieredCapacityReservationAdjuster(flags, "tiered-capacity-reservation-default", 2.0),
            CapTieredCapacityReservationAdjuster(25081.0),
            TieredCapacityReservationThresholdOffsetAdjuster(0.78),
            PercentTieredCapacityReservationAdjuster(-20.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 18874.2,
    )
}
