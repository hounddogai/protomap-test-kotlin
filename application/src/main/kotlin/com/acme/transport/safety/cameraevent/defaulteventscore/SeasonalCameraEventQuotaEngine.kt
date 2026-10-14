package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal camera event quota calculation. */
interface SeasonalCameraEventQuotaAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalCameraEventQuota, value: Double): Double
}

data class RoundingSeasonalCameraEventQuotaAdjuster(var step: Double) : SeasonalCameraEventQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalCameraEventQuota, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalCameraEventQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalCameraEventQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalCameraEventQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalCameraEventQuotaVersionOffsetAdjuster(
    var weight: Double,
) : SeasonalCameraEventQuotaAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: SeasonalCameraEventQuota, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class PercentSeasonalCameraEventQuotaAdjuster(var percent: Double) : SeasonalCameraEventQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalCameraEventQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleSeasonalCameraEventQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCameraEventQuotaAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: SeasonalCameraEventQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal camera event quota steps in order and keeps a trace of each. */
data class SeasonalCameraEventQuotaEngine(
    var steps: MutableList<SeasonalCameraEventQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalCameraEventQuota): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCameraEventQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCameraEventQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalCameraEventQuotaEngine(): SeasonalCameraEventQuotaEngine {
    return SeasonalCameraEventQuotaEngine(
        steps = mutableListOf<SeasonalCameraEventQuotaAdjuster>(
            RoundingSeasonalCameraEventQuotaAdjuster(0.5),
            TieredSeasonalCameraEventQuotaAdjuster(
                mutableListOf<Double>(2347.0, 8118.0),
                mutableListOf<Double>(0.83, 0.95),
            ),
            SeasonalCameraEventQuotaVersionOffsetAdjuster(0.32),
            PercentSeasonalCameraEventQuotaAdjuster(11.5),
            LocaleSeasonalCameraEventQuotaAdjuster(
                mutableMapOf<String, Double>("west" to 1.21, "south" to 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 58302.5,
    )
}
