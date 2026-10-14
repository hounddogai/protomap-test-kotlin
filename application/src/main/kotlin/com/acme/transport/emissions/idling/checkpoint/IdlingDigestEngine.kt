package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the idling digest calculation. */
interface IdlingDigestModifier {
    fun describe(): String
    fun adjust(item: IdlingDigest, value: Double): Double
}

data class IdlingDigestDurationMinutesOffsetAdjuster(var weight: Double) : IdlingDigestModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: IdlingDigest, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

data class TieredIdlingDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingIdlingDigestAdjuster(var step: Double) : IdlingDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingDigest, value: Double): Double = roundTo(value, step)
}

data class MinimumIdlingDigestAdjuster(var minimum: Double) : IdlingDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedIdlingDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelIdlingDigestAdjuster(var factors: MutableMap<String, Double>) : IdlingDigestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: IdlingDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the idling digest steps in order and keeps a trace of each. */
data class IdlingDigestEngine(
    var steps: MutableList<IdlingDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingDigest): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingDigestEngine(flags: Registry): IdlingDigestEngine {
    return IdlingDigestEngine(
        steps = mutableListOf<IdlingDigestModifier>(
            IdlingDigestDurationMinutesOffsetAdjuster(1.63),
            TieredIdlingDigestAdjuster(
                mutableListOf<Double>(5093.0, 6862.0, 8020.0, 8178.0),
                mutableListOf<Double>(1.08, 0.92, 1.0, 0.91),
            ),
            RoundingIdlingDigestAdjuster(0.05),
            MinimumIdlingDigestAdjuster(48.9),
            FlaggedIdlingDigestAdjuster(flags, "idling-digest-batched", -5.5),
            LabelIdlingDigestAdjuster(mutableMapOf<String, Double>("retail" to 0.73)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 93957.1,
    )
}
