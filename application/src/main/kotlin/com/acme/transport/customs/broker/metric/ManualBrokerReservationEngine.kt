package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual broker reservation calculation. */
interface ManualBrokerReservationFactor {
    fun describe(): String
    fun adjust(item: ManualBrokerReservation, value: Double): Double
}

data class TieredManualBrokerReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualBrokerReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualBrokerReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleManualBrokerReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualBrokerReservationFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: ManualBrokerReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualBrokerReservationAdjuster(var cap: Double) : ManualBrokerReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualBrokerReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the manual broker reservation steps in order and keeps a trace of each. */
data class ManualBrokerReservationEngine(
    var steps: MutableList<ManualBrokerReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualBrokerReservation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualBrokerReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualBrokerReservationEngine(): ManualBrokerReservationEngine {
    return ManualBrokerReservationEngine(
        steps = mutableListOf<ManualBrokerReservationFactor>(
            TieredManualBrokerReservationAdjuster(
                mutableListOf<Double>(3934.0, 4605.0),
                mutableListOf<Double>(0.86, 0.98),
            ),
            LocaleManualBrokerReservationAdjuster(
                mutableMapOf<String, Double>("west" to 0.98, "retail" to 0.81, "south" to 1.25),
            ),
            CapManualBrokerReservationAdjuster(13042.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 35134.7,
    )
}
