package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal service plan record calculation. */
interface SeasonalServicePlanRecordStep {
    fun describe(): String
    fun adjust(item: SeasonalServicePlanRecord, value: Double): Double
}

data class CapSeasonalServicePlanRecordAdjuster(var cap: Double) : SeasonalServicePlanRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalServicePlanRecord, value: Double): Double = minOf(value, cap)
}

data class MinimumSeasonalServicePlanRecordAdjuster(var minimum: Double) : SeasonalServicePlanRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalServicePlanRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalServicePlanRecordStopCountOffsetAdjuster(
    var weight: Double,
) : SeasonalServicePlanRecordStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: SeasonalServicePlanRecord, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class FlaggedSeasonalServicePlanRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalServicePlanRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalServicePlanRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSeasonalServicePlanRecordAdjuster(var percent: Double) : SeasonalServicePlanRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalServicePlanRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal service plan record steps in order and keeps a trace of each. */
data class SeasonalServicePlanRecordEngine(
    var steps: MutableList<SeasonalServicePlanRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalServicePlanRecord): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalServicePlanRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalServicePlanRecordEngine(flags: Registry): SeasonalServicePlanRecordEngine {
    return SeasonalServicePlanRecordEngine(
        steps = mutableListOf<SeasonalServicePlanRecordStep>(
            CapSeasonalServicePlanRecordAdjuster(49840.0),
            MinimumSeasonalServicePlanRecordAdjuster(35.0),
            SeasonalServicePlanRecordStopCountOffsetAdjuster(1.63),
            FlaggedSeasonalServicePlanRecordAdjuster(flags, "seasonal-service-plan-record-rolling", 8.6),
            PercentSeasonalServicePlanRecordAdjuster(1.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 15347.1,
    )
}
