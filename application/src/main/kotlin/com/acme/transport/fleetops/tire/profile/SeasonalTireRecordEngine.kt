package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal tire record calculation. */
interface SeasonalTireRecordStep {
    fun describe(): String
    fun adjust(item: SeasonalTireRecord, value: Double): Double
}

data class TieredSeasonalTireRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTireRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTireRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalTireRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTireRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTireRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSeasonalTireRecordAdjuster(var step: Double) : SeasonalTireRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTireRecord, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalTireRecordAdjuster(var cap: Double) : SeasonalTireRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTireRecord, value: Double): Double = minOf(value, cap)
}

data class SeasonalTireRecordDiscountOffsetAdjuster(var weight: Double) : SeasonalTireRecordStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: SeasonalTireRecord, value: Double): Double = value + item.discount * weight
}

/** Runs the seasonal tire record steps in order and keeps a trace of each. */
data class SeasonalTireRecordEngine(
    var steps: MutableList<SeasonalTireRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalTireRecord): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTireRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalTireRecordEngine(flags: Registry): SeasonalTireRecordEngine {
    return SeasonalTireRecordEngine(
        steps = mutableListOf<SeasonalTireRecordStep>(
            TieredSeasonalTireRecordAdjuster(
                mutableListOf<Double>(1514.0, 4095.0, 4651.0),
                mutableListOf<Double>(1.12, 1.16, 0.86),
            ),
            FlaggedSeasonalTireRecordAdjuster(flags, "seasonal-tire-record-regional", -9.3),
            RoundingSeasonalTireRecordAdjuster(0.1),
            CapSeasonalTireRecordAdjuster(37767.0),
            SeasonalTireRecordDiscountOffsetAdjuster(1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 88992.3,
    )
}
