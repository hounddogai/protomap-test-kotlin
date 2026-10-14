package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary inspection override calculation. */
interface PrimaryInspectionOverrideModifier {
    fun describe(): String
    fun adjust(item: PrimaryInspectionOverride, value: Double): Double
}

data class CapPrimaryInspectionOverrideAdjuster(var cap: Double) : PrimaryInspectionOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryInspectionOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryInspectionOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryInspectionOverrideModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryInspectionOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelPrimaryInspectionOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryInspectionOverrideModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: PrimaryInspectionOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryInspectionOverrideAdjuster(var percent: Double) : PrimaryInspectionOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryInspectionOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryInspectionOverrideAdjuster(var minimum: Double) : PrimaryInspectionOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryInspectionOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryInspectionOverrideRatioOffsetAdjuster(
    var weight: Double,
) : PrimaryInspectionOverrideModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PrimaryInspectionOverride, value: Double): Double = value + item.ratio * weight
}

/** Runs the primary inspection override steps in order and keeps a trace of each. */
data class PrimaryInspectionOverrideEngine(
    var steps: MutableList<PrimaryInspectionOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryInspectionOverride): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryInspectionOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryInspectionOverrideEngine(flags: Registry): PrimaryInspectionOverrideEngine {
    return PrimaryInspectionOverrideEngine(
        steps = mutableListOf<PrimaryInspectionOverrideModifier>(
            CapPrimaryInspectionOverrideAdjuster(13700.0),
            FlaggedPrimaryInspectionOverrideAdjuster(flags, "primary-inspection-override-rolling", -6.9),
            ChannelPrimaryInspectionOverrideAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "east" to 0.83),
            ),
            PercentPrimaryInspectionOverrideAdjuster(1.0),
            MinimumPrimaryInspectionOverrideAdjuster(2.9),
            PrimaryInspectionOverrideRatioOffsetAdjuster(0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 59303.8,
    )
}
