package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay window calculation. */
interface RelayWindowFactor {
    fun describe(): String
    fun adjust(item: RelayWindow, value: Double): Double
}

data class FlaggedRelayWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayWindowFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRelayWindowAdjuster(var percent: Double) : RelayWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRelayWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayWindowFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRelayWindowAdjuster(var step: Double) : RelayWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayWindow, value: Double): Double = roundTo(value, step)
}

data class RelayWindowDurationMinutesOffsetAdjuster(var weight: Double) : RelayWindowFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RelayWindow, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

data class CountryRelayWindowAdjuster(var factors: MutableMap<String, Double>) : RelayWindowFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: RelayWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the relay window steps in order and keeps a trace of each. */
data class RelayWindowEngine(
    var steps: MutableList<RelayWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayWindow): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayWindowEngine(flags: Registry): RelayWindowEngine {
    return RelayWindowEngine(
        steps = mutableListOf<RelayWindowFactor>(
            FlaggedRelayWindowAdjuster(flags, "relay-window-regional", -5.7),
            PercentRelayWindowAdjuster(4.9),
            TieredRelayWindowAdjuster(
                mutableListOf<Double>(100.0, 6957.0, 8016.0),
                mutableListOf<Double>(1.13, 0.84, 0.93),
            ),
            RoundingRelayWindowAdjuster(5.0),
            RelayWindowDurationMinutesOffsetAdjuster(0.42),
            CountryRelayWindowAdjuster(
                mutableMapOf<String, Double>("central" to 1.14, "south" to 1.09, "online" to 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 26070.0,
    )
}
