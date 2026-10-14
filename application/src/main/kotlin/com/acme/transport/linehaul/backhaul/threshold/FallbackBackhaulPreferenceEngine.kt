package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback backhaul preference calculation. */
interface FallbackBackhaulPreferenceFactor {
    fun describe(): String
    fun adjust(item: FallbackBackhaulPreference, value: Double): Double
}

data class FallbackBackhaulPreferenceFuelLitersOffsetAdjuster(
    var weight: Double,
) : FallbackBackhaulPreferenceFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: FallbackBackhaulPreference, value: Double): Double = value + item.fuelLiters * weight
}

data class RoundingFallbackBackhaulPreferenceAdjuster(var step: Double) : FallbackBackhaulPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackBackhaulPreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackBackhaulPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackBackhaulPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackBackhaulPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback backhaul preference steps in order and keeps a trace of each. */
data class FallbackBackhaulPreferenceEngine(
    var steps: MutableList<FallbackBackhaulPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackBackhaulPreference): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackBackhaulPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackBackhaulPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackBackhaulPreferenceEngine(flags: Registry): FallbackBackhaulPreferenceEngine {
    return FallbackBackhaulPreferenceEngine(
        steps = mutableListOf<FallbackBackhaulPreferenceFactor>(
            FallbackBackhaulPreferenceFuelLitersOffsetAdjuster(0.5),
            RoundingFallbackBackhaulPreferenceAdjuster(1.0),
            FlaggedFallbackBackhaulPreferenceAdjuster(flags, "fallback-backhaul-preference-scheduled", 5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 91988.3,
    )
}
