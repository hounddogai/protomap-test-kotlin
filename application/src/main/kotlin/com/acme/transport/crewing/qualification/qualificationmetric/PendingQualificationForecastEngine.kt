package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.flags.Registry

/** One step of the pending qualification forecast calculation. */
interface PendingQualificationForecastAdjuster {
    fun describe(): String
    fun adjust(item: PendingQualificationForecast, value: Double): Double
}

data class TitlePendingQualificationForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingQualificationForecastAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingQualificationForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PendingQualificationForecastScoreOffsetAdjuster(
    var weight: Double,
) : PendingQualificationForecastAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: PendingQualificationForecast, value: Double): Double = value + item.score * weight
}

data class TieredPendingQualificationForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingQualificationForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingQualificationForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPendingQualificationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingQualificationForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingQualificationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingQualificationForecastAdjuster(
    var minimum: Double,
) : PendingQualificationForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingQualificationForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingQualificationForecastAdjuster(var cap: Double) : PendingQualificationForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingQualificationForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the pending qualification forecast steps in order and keeps a trace of each. */
data class PendingQualificationForecastEngine(
    var steps: MutableList<PendingQualificationForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingQualificationForecast): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingQualificationForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingQualificationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingQualificationForecastEngine(flags: Registry): PendingQualificationForecastEngine {
    return PendingQualificationForecastEngine(
        steps = mutableListOf<PendingQualificationForecastAdjuster>(
            TitlePendingQualificationForecastAdjuster(
                mutableMapOf<String, Double>("west" to 0.98, "north" to 0.87, "east" to 0.93),
            ),
            PendingQualificationForecastScoreOffsetAdjuster(1.97),
            TieredPendingQualificationForecastAdjuster(
                mutableListOf<Double>(343.0, 5179.0, 7686.0, 9096.0),
                mutableListOf<Double>(0.81, 0.83, 0.91, 1.07),
            ),
            FlaggedPendingQualificationForecastAdjuster(flags, "pending-qualification-forecast-weighted", 1.8),
            MinimumPendingQualificationForecastAdjuster(14.2),
            CapPendingQualificationForecastAdjuster(44735.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 20179.6,
    )
}
