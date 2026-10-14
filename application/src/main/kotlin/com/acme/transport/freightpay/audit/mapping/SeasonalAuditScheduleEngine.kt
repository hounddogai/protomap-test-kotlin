package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal audit schedule calculation. */
interface SeasonalAuditScheduleStep {
    fun describe(): String
    fun adjust(item: SeasonalAuditSchedule, value: Double): Double
}

data class PercentSeasonalAuditScheduleAdjuster(var percent: Double) : SeasonalAuditScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalAuditSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SeasonalAuditScheduleDistanceKmOffsetAdjuster(var weight: Double) : SeasonalAuditScheduleStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: SeasonalAuditSchedule, value: Double): Double = value + item.distanceKm * weight
}

data class TieredSeasonalAuditScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAuditScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAuditSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleSeasonalAuditScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalAuditScheduleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalAuditSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedSeasonalAuditScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalAuditScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalAuditSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal audit schedule steps in order and keeps a trace of each. */
data class SeasonalAuditScheduleEngine(
    var steps: MutableList<SeasonalAuditScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalAuditSchedule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalAuditSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalAuditScheduleEngine(flags: Registry): SeasonalAuditScheduleEngine {
    return SeasonalAuditScheduleEngine(
        steps = mutableListOf<SeasonalAuditScheduleStep>(
            PercentSeasonalAuditScheduleAdjuster(-13.6),
            SeasonalAuditScheduleDistanceKmOffsetAdjuster(0.28),
            TieredSeasonalAuditScheduleAdjuster(
                mutableListOf<Double>(8186.0, 8705.0),
                mutableListOf<Double>(1.01, 1.02),
            ),
            TitleSeasonalAuditScheduleAdjuster(
                mutableMapOf<String, Double>("west" to 1.09, "north" to 0.88, "east" to 0.85),
            ),
            FlaggedSeasonalAuditScheduleAdjuster(flags, "seasonal-audit-schedule-primary", -4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 24511.1,
    )
}
