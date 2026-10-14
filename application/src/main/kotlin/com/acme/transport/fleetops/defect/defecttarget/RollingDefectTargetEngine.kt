package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling defect target calculation. */
interface RollingDefectTargetAdjuster {
    fun describe(): String
    fun adjust(item: RollingDefectTarget, value: Double): Double
}

data class RollingDefectTargetEndsAtOffsetAdjuster(var weight: Double) : RollingDefectTargetAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RollingDefectTarget, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class FlaggedRollingDefectTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDefectTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDefectTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRollingDefectTargetAdjuster(var minimum: Double) : RollingDefectTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDefectTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRollingDefectTargetAdjuster(var percent: Double) : RollingDefectTargetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDefectTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingDefectTargetAdjuster(var step: Double) : RollingDefectTargetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDefectTarget, value: Double): Double = roundTo(value, step)
}

data class CountryRollingDefectTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDefectTargetAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: RollingDefectTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling defect target steps in order and keeps a trace of each. */
data class RollingDefectTargetEngine(
    var steps: MutableList<RollingDefectTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingDefectTarget): Double {
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

    fun evaluateAll(items: MutableList<RollingDefectTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingDefectTargetEngine(flags: Registry): RollingDefectTargetEngine {
    return RollingDefectTargetEngine(
        steps = mutableListOf<RollingDefectTargetAdjuster>(
            RollingDefectTargetEndsAtOffsetAdjuster(1.79),
            FlaggedRollingDefectTargetAdjuster(flags, "rolling-defect-target-strict", -7.4),
            MinimumRollingDefectTargetAdjuster(33.5),
            PercentRollingDefectTargetAdjuster(-8.6),
            RoundingRollingDefectTargetAdjuster(0.1),
            CountryRollingDefectTargetAdjuster(mutableMapOf<String, Double>("east" to 1.12)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 75457.0,
    )
}
