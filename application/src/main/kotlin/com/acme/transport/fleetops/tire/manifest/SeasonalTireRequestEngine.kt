package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal tire request calculation. */
interface SeasonalTireRequestAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTireRequest, value: Double): Double
}

data class ChannelSeasonalTireRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTireRequestAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: SeasonalTireRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalTireRequestAdjuster(var percent: Double) : SeasonalTireRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTireRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSeasonalTireRequestAdjuster(var cap: Double) : SeasonalTireRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTireRequest, value: Double): Double = minOf(value, cap)
}

data class SeasonalTireRequestUpdatedAtOffsetAdjuster(var weight: Double) : SeasonalTireRequestAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: SeasonalTireRequest, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

/** Runs the seasonal tire request steps in order and keeps a trace of each. */
data class SeasonalTireRequestEngine(
    var steps: MutableList<SeasonalTireRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalTireRequest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTireRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalTireRequestEngine(): SeasonalTireRequestEngine {
    return SeasonalTireRequestEngine(
        steps = mutableListOf<SeasonalTireRequestAdjuster>(
            ChannelSeasonalTireRequestAdjuster(mutableMapOf<String, Double>("online" to 0.8)),
            PercentSeasonalTireRequestAdjuster(-18.8),
            CapSeasonalTireRequestAdjuster(2791.0),
            SeasonalTireRequestUpdatedAtOffsetAdjuster(1.21),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 11173.4,
    )
}
