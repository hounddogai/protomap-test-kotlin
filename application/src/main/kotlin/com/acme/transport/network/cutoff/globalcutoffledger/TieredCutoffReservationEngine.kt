package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered cutoff reservation calculation. */
interface TieredCutoffReservationStep {
    fun describe(): String
    fun adjust(item: TieredCutoffReservation, value: Double): Double
}

data class RoundingTieredCutoffReservationAdjuster(var step: Double) : TieredCutoffReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCutoffReservation, value: Double): Double = roundTo(value, step)
}

data class LocaleTieredCutoffReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredCutoffReservationStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: TieredCutoffReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredCutoffReservationAdjuster(var minimum: Double) : TieredCutoffReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCutoffReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTieredCutoffReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCutoffReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCutoffReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TieredCutoffReservationCreatedAtOffsetAdjuster(var weight: Double) : TieredCutoffReservationStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: TieredCutoffReservation, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

/** Runs the tiered cutoff reservation steps in order and keeps a trace of each. */
data class TieredCutoffReservationEngine(
    var steps: MutableList<TieredCutoffReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredCutoffReservation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredCutoffReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredCutoffReservationEngine(): TieredCutoffReservationEngine {
    return TieredCutoffReservationEngine(
        steps = mutableListOf<TieredCutoffReservationStep>(
            RoundingTieredCutoffReservationAdjuster(0.5),
            LocaleTieredCutoffReservationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.02, "north" to 0.77),
            ),
            MinimumTieredCutoffReservationAdjuster(12.4),
            TieredTieredCutoffReservationAdjuster(
                mutableListOf<Double>(1801.0, 4014.0, 6998.0),
                mutableListOf<Double>(1.03, 0.89, 0.99),
            ),
            TieredCutoffReservationCreatedAtOffsetAdjuster(1.74),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 22851.2,
    )
}
