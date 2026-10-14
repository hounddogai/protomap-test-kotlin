package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered availability reservation calculation. */
interface TieredAvailabilityReservationAdjuster {
    fun describe(): String
    fun adjust(item: TieredAvailabilityReservation, value: Double): Double
}

data class TitleTieredAvailabilityReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAvailabilityReservationAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredAvailabilityReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredAvailabilityReservationAdjuster(
    var step: Double,
) : TieredAvailabilityReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredAvailabilityReservation, value: Double): Double = roundTo(value, step)
}

data class TieredAvailabilityReservationRetriesOffsetAdjuster(
    var weight: Double,
) : TieredAvailabilityReservationAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: TieredAvailabilityReservation, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

/** Runs the tiered availability reservation steps in order and keeps a trace of each. */
data class TieredAvailabilityReservationEngine(
    var steps: MutableList<TieredAvailabilityReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredAvailabilityReservation): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredAvailabilityReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredAvailabilityReservationEngine(): TieredAvailabilityReservationEngine {
    return TieredAvailabilityReservationEngine(
        steps = mutableListOf<TieredAvailabilityReservationAdjuster>(
            TitleTieredAvailabilityReservationAdjuster(
                mutableMapOf<String, Double>("retail" to 0.82, "east" to 0.88, "central" to 1.39),
            ),
            RoundingTieredAvailabilityReservationAdjuster(0.05),
            TieredAvailabilityReservationRetriesOffsetAdjuster(0.14),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 71992.8,
    )
}
