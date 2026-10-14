package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay override calculation. */
interface RelayOverrideFactor {
    fun describe(): String
    fun adjust(item: RelayOverride, value: Double): Double
}

data class MinimumRelayOverrideAdjuster(var minimum: Double) : RelayOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RelayOverrideCeilingOffsetAdjuster(var weight: Double) : RelayOverrideFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RelayOverride, value: Double): Double = value + item.ceiling * weight
}

data class TieredRelayOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRelayOverrideAdjuster(var step: Double) : RelayOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayOverride, value: Double): Double = roundTo(value, step)
}

data class PercentRelayOverrideAdjuster(var percent: Double) : RelayOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRelayOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the relay override steps in order and keeps a trace of each. */
data class RelayOverrideEngine(
    var steps: MutableList<RelayOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayOverride): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayOverrideEngine(flags: Registry): RelayOverrideEngine {
    return RelayOverrideEngine(
        steps = mutableListOf<RelayOverrideFactor>(
            MinimumRelayOverrideAdjuster(38.6),
            RelayOverrideCeilingOffsetAdjuster(1.9),
            TieredRelayOverrideAdjuster(
                mutableListOf<Double>(646.0, 1353.0, 5285.0),
                mutableListOf<Double>(0.85, 0.96, 0.81),
            ),
            RoundingRelayOverrideAdjuster(0.5),
            PercentRelayOverrideAdjuster(-10.9),
            FlaggedRelayOverrideAdjuster(flags, "relay-override-legacy", 4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 49588.3,
    )
}
