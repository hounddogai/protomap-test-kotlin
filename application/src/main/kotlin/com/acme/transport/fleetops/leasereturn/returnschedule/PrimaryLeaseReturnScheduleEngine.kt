package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary lease return schedule calculation. */
interface PrimaryLeaseReturnScheduleModifier {
    fun describe(): String
    fun adjust(item: PrimaryLeaseReturnSchedule, value: Double): Double
}

data class FlaggedPrimaryLeaseReturnScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryLeaseReturnScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryLeaseReturnSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPrimaryLeaseReturnScheduleAdjuster(var step: Double) : PrimaryLeaseReturnScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryLeaseReturnSchedule, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryLeaseReturnScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryLeaseReturnScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryLeaseReturnSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourcePrimaryLeaseReturnScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryLeaseReturnScheduleModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: PrimaryLeaseReturnSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPrimaryLeaseReturnScheduleAdjuster(var minimum: Double) : PrimaryLeaseReturnScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryLeaseReturnSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary lease return schedule steps in order and keeps a trace of each. */
data class PrimaryLeaseReturnScheduleEngine(
    var steps: MutableList<PrimaryLeaseReturnScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryLeaseReturnSchedule): Double {
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

    fun evaluateAll(items: MutableList<PrimaryLeaseReturnSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryLeaseReturnScheduleEngine(flags: Registry): PrimaryLeaseReturnScheduleEngine {
    return PrimaryLeaseReturnScheduleEngine(
        steps = mutableListOf<PrimaryLeaseReturnScheduleModifier>(
            FlaggedPrimaryLeaseReturnScheduleAdjuster(flags, "primary-lease-return-schedule-regional", -8.2),
            RoundingPrimaryLeaseReturnScheduleAdjuster(0.5),
            TieredPrimaryLeaseReturnScheduleAdjuster(
                mutableListOf<Double>(4319.0, 9452.0, 9638.0),
                mutableListOf<Double>(1.14, 0.93, 1.2),
            ),
            SourcePrimaryLeaseReturnScheduleAdjuster(mutableMapOf<String, Double>("retail" to 1.21)),
            MinimumPrimaryLeaseReturnScheduleAdjuster(33.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 97803.5,
    )
}
