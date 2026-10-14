package com.acme.transport.network.transittime.score

import com.acme.transport.kit.flags.Registry

/** One step of the fallback transit time baseline calculation. */
interface FallbackTransitTimeBaselineAdjuster {
    fun describe(): String
    fun adjust(item: FallbackTransitTimeBaseline, value: Double): Double
}

data class LocaleFallbackTransitTimeBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackTransitTimeBaselineAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: FallbackTransitTimeBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackTransitTimeBaselineAdjuster(
    var minimum: Double,
) : FallbackTransitTimeBaselineAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTransitTimeBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackTransitTimeBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackTransitTimeBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackTransitTimeBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackTransitTimeBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackTransitTimeBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackTransitTimeBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackTransitTimeBaselineAdjuster(
    var percent: Double,
) : FallbackTransitTimeBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTransitTimeBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FallbackTransitTimeBaselineSequenceOffsetAdjuster(
    var weight: Double,
) : FallbackTransitTimeBaselineAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: FallbackTransitTimeBaseline, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

/** Runs the fallback transit time baseline steps in order and keeps a trace of each. */
data class FallbackTransitTimeBaselineEngine(
    var steps: MutableList<FallbackTransitTimeBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackTransitTimeBaseline): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackTransitTimeBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackTransitTimeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackTransitTimeBaselineEngine(flags: Registry): FallbackTransitTimeBaselineEngine {
    return FallbackTransitTimeBaselineEngine(
        steps = mutableListOf<FallbackTransitTimeBaselineAdjuster>(
            LocaleFallbackTransitTimeBaselineAdjuster(
                mutableMapOf<String, Double>("online" to 1.35, "retail" to 1.17),
            ),
            MinimumFallbackTransitTimeBaselineAdjuster(41.8),
            FlaggedFallbackTransitTimeBaselineAdjuster(flags, "fallback-transit-time-baseline-tiered", 2.3),
            TieredFallbackTransitTimeBaselineAdjuster(
                mutableListOf<Double>(57.0, 924.0, 1207.0, 6140.0),
                mutableListOf<Double>(1.01, 0.82, 1.09, 0.85),
            ),
            PercentFallbackTransitTimeBaselineAdjuster(-12.2),
            FallbackTransitTimeBaselineSequenceOffsetAdjuster(0.16),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 36709.6,
    )
}
