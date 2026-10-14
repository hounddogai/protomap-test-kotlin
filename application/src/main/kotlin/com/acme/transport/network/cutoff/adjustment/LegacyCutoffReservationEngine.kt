package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy cutoff reservation calculation. */
interface LegacyCutoffReservationAdjuster {
    fun describe(): String
    fun adjust(item: LegacyCutoffReservation, value: Double): Double
}

data class LegacyCutoffReservationCostOffsetAdjuster(var weight: Double) : LegacyCutoffReservationAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: LegacyCutoffReservation, value: Double): Double = value + item.cost * weight
}

data class LabelLegacyCutoffReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyCutoffReservationAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyCutoffReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyCutoffReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyCutoffReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyCutoffReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy cutoff reservation steps in order and keeps a trace of each. */
data class LegacyCutoffReservationEngine(
    var steps: MutableList<LegacyCutoffReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyCutoffReservation): Double {
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

    fun evaluateAll(items: MutableList<LegacyCutoffReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyCutoffReservationEngine(): LegacyCutoffReservationEngine {
    return LegacyCutoffReservationEngine(
        steps = mutableListOf<LegacyCutoffReservationAdjuster>(
            LegacyCutoffReservationCostOffsetAdjuster(0.16),
            LabelLegacyCutoffReservationAdjuster(mutableMapOf<String, Double>("east" to 1.0, "retail" to 1.13)),
            TieredLegacyCutoffReservationAdjuster(
                mutableListOf<Double>(2264.0, 3745.0, 8963.0),
                mutableListOf<Double>(1.12, 0.83, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 79835.5,
    )
}
