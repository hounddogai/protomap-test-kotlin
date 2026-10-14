package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient relay group calculation. */
interface LenientRelayGroupAdjuster {
    fun describe(): String
    fun adjust(item: LenientRelayGroup, value: Double): Double
}

data class TitleLenientRelayGroupAdjuster(var factors: MutableMap<String, Double>) : LenientRelayGroupAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientRelayGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientRelayGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientRelayGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientRelayGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientRelayGroupAdjuster(var minimum: Double) : LenientRelayGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRelayGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LenientRelayGroupExpiresAtOffsetAdjuster(var weight: Double) : LenientRelayGroupAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: LenientRelayGroup, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class FlaggedLenientRelayGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientRelayGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientRelayGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLenientRelayGroupAdjuster(var step: Double) : LenientRelayGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientRelayGroup, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient relay group steps in order and keeps a trace of each. */
data class LenientRelayGroupEngine(
    var steps: MutableList<LenientRelayGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientRelayGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientRelayGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientRelayGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientRelayGroupEngine(flags: Registry): LenientRelayGroupEngine {
    return LenientRelayGroupEngine(
        steps = mutableListOf<LenientRelayGroupAdjuster>(
            TitleLenientRelayGroupAdjuster(mutableMapOf<String, Double>("south" to 1.21, "retail" to 0.85)),
            TieredLenientRelayGroupAdjuster(
                mutableListOf<Double>(19.0, 791.0, 3122.0, 7114.0),
                mutableListOf<Double>(0.81, 1.0, 0.86, 0.97),
            ),
            MinimumLenientRelayGroupAdjuster(0.2),
            LenientRelayGroupExpiresAtOffsetAdjuster(1.93),
            FlaggedLenientRelayGroupAdjuster(flags, "lenient-relay-group-fallback", 10.0),
            RoundingLenientRelayGroupAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 34346.6,
    )
}
