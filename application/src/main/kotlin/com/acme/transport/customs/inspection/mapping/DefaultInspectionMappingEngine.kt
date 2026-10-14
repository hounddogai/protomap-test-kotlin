package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the default inspection mapping calculation. */
interface DefaultInspectionMappingStep {
    fun describe(): String
    fun adjust(item: DefaultInspectionMapping, value: Double): Double
}

data class FlaggedDefaultInspectionMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultInspectionMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultInspectionMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefaultInspectionMappingAdjuster(var minimum: Double) : DefaultInspectionMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultInspectionMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultInspectionMappingSequenceOffsetAdjuster(var weight: Double) : DefaultInspectionMappingStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: DefaultInspectionMapping, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class LabelDefaultInspectionMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultInspectionMappingStep {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultInspectionMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default inspection mapping steps in order and keeps a trace of each. */
data class DefaultInspectionMappingEngine(
    var steps: MutableList<DefaultInspectionMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultInspectionMapping): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultInspectionMappingStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultInspectionMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultInspectionMappingEngine(flags: Registry): DefaultInspectionMappingEngine {
    return DefaultInspectionMappingEngine(
        steps = mutableListOf<DefaultInspectionMappingStep>(
            FlaggedDefaultInspectionMappingAdjuster(flags, "default-inspection-mapping-global", 8.9),
            MinimumDefaultInspectionMappingAdjuster(19.6),
            DefaultInspectionMappingSequenceOffsetAdjuster(1.17),
            LabelDefaultInspectionMappingAdjuster(
                mutableMapOf<String, Double>("north" to 1.36, "south" to 0.75, "online" to 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 14852.3,
    )
}
