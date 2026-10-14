package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped dwell schedule calculation. */
interface CappedDwellScheduleFactor {
    fun describe(): String
    fun adjust(item: CappedDwellSchedule, value: Double): Double
}

data class CappedDwellScheduleEtaMinutesOffsetAdjuster(var weight: Double) : CappedDwellScheduleFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: CappedDwellSchedule, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class LabelCappedDwellScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedDwellScheduleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedDwellSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedDwellScheduleAdjuster(var minimum: Double) : CappedDwellScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDwellSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedDwellScheduleAdjuster(var percent: Double) : CappedDwellScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDwellSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedDwellScheduleAdjuster(var step: Double) : CappedDwellScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDwellSchedule, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedDwellScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDwellScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDwellSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped dwell schedule steps in order and keeps a trace of each. */
data class CappedDwellScheduleEngine(
    var steps: MutableList<CappedDwellScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedDwellSchedule): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDwellSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedDwellScheduleEngine(flags: Registry): CappedDwellScheduleEngine {
    return CappedDwellScheduleEngine(
        steps = mutableListOf<CappedDwellScheduleFactor>(
            CappedDwellScheduleEtaMinutesOffsetAdjuster(0.7),
            LabelCappedDwellScheduleAdjuster(
                mutableMapOf<String, Double>("south" to 0.71, "online" to 1.21, "east" to 1.15),
            ),
            MinimumCappedDwellScheduleAdjuster(33.4),
            PercentCappedDwellScheduleAdjuster(-5.0),
            RoundingCappedDwellScheduleAdjuster(0.1),
            FlaggedCappedDwellScheduleAdjuster(flags, "capped-dwell-schedule-manual", 4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 59436.0,
    )
}
