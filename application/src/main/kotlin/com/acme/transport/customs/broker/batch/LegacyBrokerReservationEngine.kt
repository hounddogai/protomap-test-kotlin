package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy broker reservation calculation. */
interface LegacyBrokerReservationModifier {
    fun describe(): String
    fun adjust(item: LegacyBrokerReservation, value: Double): Double
}

data class LabelLegacyBrokerReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyBrokerReservationModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyBrokerReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyBrokerReservationAdjuster(var percent: Double) : LegacyBrokerReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyBrokerReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LegacyBrokerReservationSequenceOffsetAdjuster(var weight: Double) : LegacyBrokerReservationModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: LegacyBrokerReservation, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

/** Runs the legacy broker reservation steps in order and keeps a trace of each. */
data class LegacyBrokerReservationEngine(
    var steps: MutableList<LegacyBrokerReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyBrokerReservation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyBrokerReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyBrokerReservationEngine(): LegacyBrokerReservationEngine {
    return LegacyBrokerReservationEngine(
        steps = mutableListOf<LegacyBrokerReservationModifier>(
            LabelLegacyBrokerReservationAdjuster(
                mutableMapOf<String, Double>("west" to 0.81, "central" to 0.72, "east" to 1.05),
            ),
            PercentLegacyBrokerReservationAdjuster(-18.7),
            LegacyBrokerReservationSequenceOffsetAdjuster(0.75),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 67964.6,
    )
}
