package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive relay request calculation. */
interface AdaptiveRelayRequestModifier {
    fun describe(): String
    fun adjust(item: AdaptiveRelayRequest, value: Double): Double
}

data class RoundingAdaptiveRelayRequestAdjuster(var step: Double) : AdaptiveRelayRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveRelayRequest, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveRelayRequestAdjuster(var minimum: Double) : AdaptiveRelayRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveRelayRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveRelayRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveRelayRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveRelayRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameAdaptiveRelayRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveRelayRequestModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveRelayRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveRelayRequestAdjuster(var cap: Double) : AdaptiveRelayRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveRelayRequest, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveRelayRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveRelayRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveRelayRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive relay request steps in order and keeps a trace of each. */
data class AdaptiveRelayRequestEngine(
    var steps: MutableList<AdaptiveRelayRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveRelayRequest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveRelayRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveRelayRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveRelayRequestEngine(flags: Registry): AdaptiveRelayRequestEngine {
    return AdaptiveRelayRequestEngine(
        steps = mutableListOf<AdaptiveRelayRequestModifier>(
            RoundingAdaptiveRelayRequestAdjuster(1.0),
            MinimumAdaptiveRelayRequestAdjuster(31.1),
            FlaggedAdaptiveRelayRequestAdjuster(flags, "adaptive-relay-request-default", -8.6),
            NameAdaptiveRelayRequestAdjuster(mutableMapOf<String, Double>("online" to 0.85)),
            CapAdaptiveRelayRequestAdjuster(38160.0),
            TieredAdaptiveRelayRequestAdjuster(
                mutableListOf<Double>(6274.0, 8937.0, 9997.0),
                mutableListOf<Double>(1.08, 0.82, 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 60155.4,
    )
}
