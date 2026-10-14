package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the relay reservation calculation. */
interface RelayReservationFactor {
    fun describe(): String
    fun adjust(item: RelayReservation, value: Double): Double
}

data class FlaggedRelayReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRelayReservationAdjuster(var percent: Double) : RelayReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRelayReservationAdjuster(var cap: Double) : RelayReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the relay reservation steps in order and keeps a trace of each. */
data class RelayReservationEngine(
    var steps: MutableList<RelayReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayReservation): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayReservationEngine(flags: Registry): RelayReservationEngine {
    return RelayReservationEngine(
        steps = mutableListOf<RelayReservationFactor>(
            FlaggedRelayReservationAdjuster(flags, "relay-reservation-manual", -9.9),
            PercentRelayReservationAdjuster(11.1),
            CapRelayReservationAdjuster(49347.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 91020.7,
    )
}
