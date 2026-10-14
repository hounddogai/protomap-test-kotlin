package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict idling score calculation. */
interface StrictIdlingScoreAdjuster {
    fun describe(): String
    fun adjust(item: StrictIdlingScore, value: Double): Double
}

data class RoundingStrictIdlingScoreAdjuster(var step: Double) : StrictIdlingScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictIdlingScore, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictIdlingScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictIdlingScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictIdlingScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictIdlingScoreAdjuster(var minimum: Double) : StrictIdlingScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictIdlingScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelStrictIdlingScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictIdlingScoreAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: StrictIdlingScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictIdlingScoreAdjuster(var percent: Double) : StrictIdlingScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictIdlingScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict idling score steps in order and keeps a trace of each. */
data class StrictIdlingScoreEngine(
    var steps: MutableList<StrictIdlingScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictIdlingScore): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictIdlingScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictIdlingScoreEngine(flags: Registry): StrictIdlingScoreEngine {
    return StrictIdlingScoreEngine(
        steps = mutableListOf<StrictIdlingScoreAdjuster>(
            RoundingStrictIdlingScoreAdjuster(1.0),
            FlaggedStrictIdlingScoreAdjuster(flags, "strict-idling-score-fallback", -3.7),
            MinimumStrictIdlingScoreAdjuster(31.8),
            ChannelStrictIdlingScoreAdjuster(
                mutableMapOf<String, Double>("central" to 0.79, "online" to 1.39, "east" to 0.71),
            ),
            PercentStrictIdlingScoreAdjuster(21.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 59437.8,
    )
}
