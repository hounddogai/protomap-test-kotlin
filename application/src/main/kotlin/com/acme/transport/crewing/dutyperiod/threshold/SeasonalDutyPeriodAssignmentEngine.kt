package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal duty period assignment calculation. */
interface SeasonalDutyPeriodAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalDutyPeriodAssignment, value: Double): Double
}

data class NameSeasonalDutyPeriodAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalDutyPeriodAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalDutyPeriodAssignmentAdjuster(
    var step: Double,
) : SeasonalDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDutyPeriodAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalDutyPeriodAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDutyPeriodAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalDutyPeriodAssignmentFuelLitersOffsetAdjuster(
    var weight: Double,
) : SeasonalDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: SeasonalDutyPeriodAssignment, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedSeasonalDutyPeriodAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDutyPeriodAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDutyPeriodAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal duty period assignment steps in order and keeps a trace of each. */
data class SeasonalDutyPeriodAssignmentEngine(
    var steps: MutableList<SeasonalDutyPeriodAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalDutyPeriodAssignment): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalDutyPeriodAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalDutyPeriodAssignmentEngine(flags: Registry): SeasonalDutyPeriodAssignmentEngine {
    return SeasonalDutyPeriodAssignmentEngine(
        steps = mutableListOf<SeasonalDutyPeriodAssignmentAdjuster>(
            NameSeasonalDutyPeriodAssignmentAdjuster(
                mutableMapOf<String, Double>("north" to 1.24, "central" to 1.04),
            ),
            RoundingSeasonalDutyPeriodAssignmentAdjuster(5.0),
            TieredSeasonalDutyPeriodAssignmentAdjuster(
                mutableListOf<Double>(525.0, 3540.0, 9624.0),
                mutableListOf<Double>(1.04, 0.93, 1.16),
            ),
            SeasonalDutyPeriodAssignmentFuelLitersOffsetAdjuster(1.02),
            FlaggedSeasonalDutyPeriodAssignmentAdjuster(flags, "seasonal-duty-period-assignment-lenient", -7.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 9983.6,
    )
}
