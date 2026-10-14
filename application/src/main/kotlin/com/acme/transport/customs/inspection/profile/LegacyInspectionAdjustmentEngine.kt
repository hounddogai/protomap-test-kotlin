package com.acme.transport.customs.inspection.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy inspection adjustment calculation. */
interface LegacyInspectionAdjustmentFactor {
    fun describe(): String
    fun adjust(item: LegacyInspectionAdjustment, value: Double): Double
}

data class TitleLegacyInspectionAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyInspectionAdjustmentFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyInspectionAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyInspectionAdjustmentAdjuster(var step: Double) : LegacyInspectionAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyInspectionAdjustment, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyInspectionAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyInspectionAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyInspectionAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy inspection adjustment steps in order and keeps a trace of each. */
data class LegacyInspectionAdjustmentEngine(
    var steps: MutableList<LegacyInspectionAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyInspectionAdjustment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyInspectionAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyInspectionAdjustmentEngine(flags: Registry): LegacyInspectionAdjustmentEngine {
    return LegacyInspectionAdjustmentEngine(
        steps = mutableListOf<LegacyInspectionAdjustmentFactor>(
            TitleLegacyInspectionAdjustmentAdjuster(
                mutableMapOf<String, Double>("west" to 0.99, "central" to 1.18, "retail" to 0.8),
            ),
            RoundingLegacyInspectionAdjustmentAdjuster(0.5),
            FlaggedLegacyInspectionAdjustmentAdjuster(flags, "legacy-inspection-adjustment-capped", 4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 31161.2,
    )
}
