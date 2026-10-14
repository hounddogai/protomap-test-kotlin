package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy lease return reservation calculation. */
interface LegacyLeaseReturnReservationAdjuster {
    fun describe(): String
    fun adjust(item: LegacyLeaseReturnReservation, value: Double): Double
}

data class TieredLegacyLeaseReturnReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyLeaseReturnReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyLeaseReturnReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LegacyLeaseReturnReservationMarginOffsetAdjuster(
    var weight: Double,
) : LegacyLeaseReturnReservationAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LegacyLeaseReturnReservation, value: Double): Double = value + item.margin * weight
}

data class CapLegacyLeaseReturnReservationAdjuster(var cap: Double) : LegacyLeaseReturnReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLeaseReturnReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy lease return reservation steps in order and keeps a trace of each. */
data class LegacyLeaseReturnReservationEngine(
    var steps: MutableList<LegacyLeaseReturnReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyLeaseReturnReservation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyLeaseReturnReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyLeaseReturnReservationEngine(): LegacyLeaseReturnReservationEngine {
    return LegacyLeaseReturnReservationEngine(
        steps = mutableListOf<LegacyLeaseReturnReservationAdjuster>(
            TieredLegacyLeaseReturnReservationAdjuster(
                mutableListOf<Double>(1198.0, 3481.0, 8266.0, 9686.0),
                mutableListOf<Double>(0.9, 1.0, 0.89, 1.1),
            ),
            LegacyLeaseReturnReservationMarginOffsetAdjuster(1.64),
            CapLegacyLeaseReturnReservationAdjuster(27628.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 38839.3,
    )
}
