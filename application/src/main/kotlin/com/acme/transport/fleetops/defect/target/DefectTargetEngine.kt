package com.acme.transport.fleetops.defect.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect target calculation. */
interface DefectTargetAdjuster {
    fun describe(): String
    fun adjust(item: DefectTarget, value: Double): Double
}

data class FlaggedDefectTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefectTargetAdjuster(var percent: Double) : DefectTargetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefectTargetExpiresAtOffsetAdjuster(var weight: Double) : DefectTargetAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: DefectTarget, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class MinimumDefectTargetAdjuster(var minimum: Double) : DefectTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefectTargetAdjuster(var cap: Double) : DefectTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the defect target steps in order and keeps a trace of each. */
data class DefectTargetEngine(
    var steps: MutableList<DefectTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefectTarget): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefectTargetEngine(flags: Registry): DefectTargetEngine {
    return DefectTargetEngine(
        steps = mutableListOf<DefectTargetAdjuster>(
            FlaggedDefectTargetAdjuster(flags, "defect-target-regional", 1.2),
            PercentDefectTargetAdjuster(16.0),
            DefectTargetExpiresAtOffsetAdjuster(1.38),
            MinimumDefectTargetAdjuster(24.3),
            CapDefectTargetAdjuster(27767.0),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 12855.5,
    )
}
