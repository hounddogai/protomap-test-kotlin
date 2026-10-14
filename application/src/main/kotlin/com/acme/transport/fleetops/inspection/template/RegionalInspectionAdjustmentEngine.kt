package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional inspection adjustment calculation. */
interface RegionalInspectionAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: RegionalInspectionAdjustment, value: Double): Double
}

data class FlaggedRegionalInspectionAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalInspectionAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalInspectionAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalInspectionAdjustmentAdjuster(var cap: Double) : RegionalInspectionAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalInspectionAdjustment, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalInspectionAdjustmentAdjuster(
    var minimum: Double,
) : RegionalInspectionAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalInspectionAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRegionalInspectionAdjustmentAdjuster(
    var step: Double,
) : RegionalInspectionAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalInspectionAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the regional inspection adjustment steps in order and keeps a trace of each. */
data class RegionalInspectionAdjustmentEngine(
    var steps: MutableList<RegionalInspectionAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalInspectionAdjustment): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalInspectionAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalInspectionAdjustmentEngine(flags: Registry): RegionalInspectionAdjustmentEngine {
    return RegionalInspectionAdjustmentEngine(
        steps = mutableListOf<RegionalInspectionAdjustmentAdjuster>(
            FlaggedRegionalInspectionAdjustmentAdjuster(flags, "regional-inspection-adjustment-legacy", -9.7),
            CapRegionalInspectionAdjustmentAdjuster(23469.0),
            MinimumRegionalInspectionAdjustmentAdjuster(29.2),
            RoundingRegionalInspectionAdjustmentAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 70911.3,
    )
}
