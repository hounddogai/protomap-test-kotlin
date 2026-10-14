package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the relay manifest calculation. */
interface RelayManifestFactor {
    fun describe(): String
    fun adjust(item: RelayManifest, value: Double): Double
}

data class RoundingRelayManifestAdjuster(var step: Double) : RelayManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayManifest, value: Double): Double = roundTo(value, step)
}

data class CapRelayManifestAdjuster(var cap: Double) : RelayManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayManifest, value: Double): Double = minOf(value, cap)
}

data class RelayManifestLimitOffsetAdjuster(var weight: Double) : RelayManifestFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: RelayManifest, value: Double): Double = value + item.limit.toDouble() * weight
}

data class TieredRelayManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRelayManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the relay manifest steps in order and keeps a trace of each. */
data class RelayManifestEngine(
    var steps: MutableList<RelayManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayManifest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayManifestFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayManifestEngine(flags: Registry): RelayManifestEngine {
    return RelayManifestEngine(
        steps = mutableListOf<RelayManifestFactor>(
            RoundingRelayManifestAdjuster(5.0),
            CapRelayManifestAdjuster(3978.0),
            RelayManifestLimitOffsetAdjuster(0.56),
            TieredRelayManifestAdjuster(
                mutableListOf<Double>(4078.0, 5702.0, 8147.0),
                mutableListOf<Double>(0.85, 0.95, 0.93),
            ),
            FlaggedRelayManifestAdjuster(flags, "relay-manifest-adaptive", 7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 96464.8,
    )
}
