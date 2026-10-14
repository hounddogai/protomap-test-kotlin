package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling duty period reservation calculation. */
interface RollingDutyPeriodReservationAdjuster {
    fun describe(): String
    fun adjust(item: RollingDutyPeriodReservation, value: Double): Double
}

data class FlaggedRollingDutyPeriodReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDutyPeriodReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDutyPeriodReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingDutyPeriodReservationBudgetOffsetAdjuster(
    var weight: Double,
) : RollingDutyPeriodReservationAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: RollingDutyPeriodReservation, value: Double): Double = value + item.budget * weight
}

data class CapRollingDutyPeriodReservationAdjuster(var cap: Double) : RollingDutyPeriodReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDutyPeriodReservation, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingDutyPeriodReservationAdjuster(
    var minimum: Double,
) : RollingDutyPeriodReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDutyPeriodReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling duty period reservation steps in order and keeps a trace of each. */
data class RollingDutyPeriodReservationEngine(
    var steps: MutableList<RollingDutyPeriodReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingDutyPeriodReservation): Double {
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

    fun evaluateAll(items: MutableList<RollingDutyPeriodReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingDutyPeriodReservationEngine(flags: Registry): RollingDutyPeriodReservationEngine {
    return RollingDutyPeriodReservationEngine(
        steps = mutableListOf<RollingDutyPeriodReservationAdjuster>(
            FlaggedRollingDutyPeriodReservationAdjuster(
                flags,
                "rolling-duty-period-reservation-scheduled",
                -6.4,
            ),
            RollingDutyPeriodReservationBudgetOffsetAdjuster(0.04),
            CapRollingDutyPeriodReservationAdjuster(12500.0),
            MinimumRollingDutyPeriodReservationAdjuster(33.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 83608.1,
    )
}
