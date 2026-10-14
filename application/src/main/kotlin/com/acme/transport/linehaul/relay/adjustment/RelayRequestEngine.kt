package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay request calculation. */
interface RelayRequestModifier {
    fun describe(): String
    fun adjust(item: RelayRequest, value: Double): Double
}

data class FlaggedRelayRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RelayRequestEndsAtOffsetAdjuster(var weight: Double) : RelayRequestModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RelayRequest, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class RoundingRelayRequestAdjuster(var step: Double) : RelayRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayRequest, value: Double): Double = roundTo(value, step)
}

/** Runs the relay request steps in order and keeps a trace of each. */
data class RelayRequestEngine(
    var steps: MutableList<RelayRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelayRequest): Double {
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

    fun evaluateAll(items: MutableList<RelayRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayRequestEngine(flags: Registry): RelayRequestEngine {
    return RelayRequestEngine(
        steps = mutableListOf<RelayRequestModifier>(
            FlaggedRelayRequestAdjuster(flags, "relay-request-primary", -2.7),
            RelayRequestEndsAtOffsetAdjuster(0.78),
            RoundingRelayRequestAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 76752.6,
    )
}
