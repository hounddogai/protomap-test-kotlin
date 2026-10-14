package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback coaching window calculation. */
interface FallbackCoachingWindowFactor {
    fun describe(): String
    fun adjust(item: FallbackCoachingWindow, value: Double): Double
}

data class FlaggedFallbackCoachingWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackCoachingWindowFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackCoachingWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackCoachingWindowAdjuster(var cap: Double) : FallbackCoachingWindowFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCoachingWindow, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackCoachingWindowAdjuster(var percent: Double) : FallbackCoachingWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCoachingWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback coaching window steps in order and keeps a trace of each. */
data class FallbackCoachingWindowEngine(
    var steps: MutableList<FallbackCoachingWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackCoachingWindow): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCoachingWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackCoachingWindowEngine(flags: Registry): FallbackCoachingWindowEngine {
    return FallbackCoachingWindowEngine(
        steps = mutableListOf<FallbackCoachingWindowFactor>(
            FlaggedFallbackCoachingWindowAdjuster(flags, "fallback-coaching-window-seasonal", 5.9),
            CapFallbackCoachingWindowAdjuster(41387.0),
            PercentFallbackCoachingWindowAdjuster(-21.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 16644.5,
    )
}
