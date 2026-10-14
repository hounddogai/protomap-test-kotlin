package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default transit time manifest calculation. */
interface DefaultTransitTimeManifestModifier {
    fun describe(): String
    fun adjust(item: DefaultTransitTimeManifest, value: Double): Double
}

data class MinimumDefaultTransitTimeManifestAdjuster(var minimum: Double) : DefaultTransitTimeManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTransitTimeManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultTransitTimeManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultTransitTimeManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultTransitTimeManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultTransitTimeManifestAdjuster(var cap: Double) : DefaultTransitTimeManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTransitTimeManifest, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultTransitTimeManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTransitTimeManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTransitTimeManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDefaultTransitTimeManifestAdjuster(var percent: Double) : DefaultTransitTimeManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultTransitTimeManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultTransitTimeManifestAdjuster(var step: Double) : DefaultTransitTimeManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultTransitTimeManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the default transit time manifest steps in order and keeps a trace of each. */
data class DefaultTransitTimeManifestEngine(
    var steps: MutableList<DefaultTransitTimeManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultTransitTimeManifest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultTransitTimeManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultTransitTimeManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultTransitTimeManifestEngine(flags: Registry): DefaultTransitTimeManifestEngine {
    return DefaultTransitTimeManifestEngine(
        steps = mutableListOf<DefaultTransitTimeManifestModifier>(
            MinimumDefaultTransitTimeManifestAdjuster(24.3),
            FlaggedDefaultTransitTimeManifestAdjuster(flags, "default-transit-time-manifest-tiered", -3.4),
            CapDefaultTransitTimeManifestAdjuster(18449.0),
            TieredDefaultTransitTimeManifestAdjuster(
                mutableListOf<Double>(7329.0, 9061.0),
                mutableListOf<Double>(0.96, 0.87),
            ),
            PercentDefaultTransitTimeManifestAdjuster(-17.4),
            RoundingDefaultTransitTimeManifestAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 24588.5,
    )
}
