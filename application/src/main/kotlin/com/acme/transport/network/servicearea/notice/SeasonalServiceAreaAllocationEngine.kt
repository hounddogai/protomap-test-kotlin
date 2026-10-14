package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal service area allocation calculation. */
interface SeasonalServiceAreaAllocationStep {
    fun describe(): String
    fun adjust(item: SeasonalServiceAreaAllocation, value: Double): Double
}

data class MinimumSeasonalServiceAreaAllocationAdjuster(
    var minimum: Double,
) : SeasonalServiceAreaAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalServiceAreaAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalServiceAreaAllocationAdjuster(var cap: Double) : SeasonalServiceAreaAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalServiceAreaAllocation, value: Double): Double = minOf(value, cap)
}

data class TitleSeasonalServiceAreaAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalServiceAreaAllocationStep {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalServiceAreaAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalServiceAreaAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalServiceAreaAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalServiceAreaAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalServiceAreaAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalServiceAreaAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalServiceAreaAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal service area allocation steps in order and keeps a trace of each. */
data class SeasonalServiceAreaAllocationEngine(
    var steps: MutableList<SeasonalServiceAreaAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalServiceAreaAllocation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalServiceAreaAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalServiceAreaAllocationEngine(flags: Registry): SeasonalServiceAreaAllocationEngine {
    return SeasonalServiceAreaAllocationEngine(
        steps = mutableListOf<SeasonalServiceAreaAllocationStep>(
            MinimumSeasonalServiceAreaAllocationAdjuster(19.6),
            CapSeasonalServiceAreaAllocationAdjuster(5563.0),
            TitleSeasonalServiceAreaAllocationAdjuster(mutableMapOf<String, Double>("central" to 1.38)),
            TieredSeasonalServiceAreaAllocationAdjuster(
                mutableListOf<Double>(6636.0, 7018.0, 8641.0),
                mutableListOf<Double>(1.17, 1.2, 0.96),
            ),
            FlaggedSeasonalServiceAreaAllocationAdjuster(
                flags,
                "seasonal-service-area-allocation-weighted",
                -4.7,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 26240.4,
    )
}
