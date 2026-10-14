package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive inspection profile calculation. */
interface AdaptiveInspectionProfileModifier {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionProfile, value: Double): Double
}

data class CapAdaptiveInspectionProfileAdjuster(var cap: Double) : AdaptiveInspectionProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionProfile, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveInspectionProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveInspectionProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveInspectionProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameAdaptiveInspectionProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveInspectionProfileModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveInspectionProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveInspectionProfileAdjuster(var percent: Double) : AdaptiveInspectionProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveInspectionProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive inspection profile steps in order and keeps a trace of each. */
data class AdaptiveInspectionProfileEngine(
    var steps: MutableList<AdaptiveInspectionProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveInspectionProfile): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionProfileEngine(flags: Registry): AdaptiveInspectionProfileEngine {
    return AdaptiveInspectionProfileEngine(
        steps = mutableListOf<AdaptiveInspectionProfileModifier>(
            CapAdaptiveInspectionProfileAdjuster(21839.0),
            FlaggedAdaptiveInspectionProfileAdjuster(flags, "adaptive-inspection-profile-adaptive", -9.9),
            NameAdaptiveInspectionProfileAdjuster(mutableMapOf<String, Double>("west" to 0.95)),
            PercentAdaptiveInspectionProfileAdjuster(-14.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 16890.3,
    )
}
