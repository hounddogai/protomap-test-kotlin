package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection override calculation. */
interface InspectionOverrideStep {
    fun describe(): String
    fun adjust(item: InspectionOverride, value: Double): Double
}

data class NameInspectionOverrideAdjuster(var factors: MutableMap<String, Double>) : InspectionOverrideStep {
    override fun describe(): String = "by name"

    override fun adjust(item: InspectionOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedInspectionOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingInspectionOverrideAdjuster(var step: Double) : InspectionOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionOverride, value: Double): Double = roundTo(value, step)
}

data class CapInspectionOverrideAdjuster(var cap: Double) : InspectionOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionOverride, value: Double): Double = minOf(value, cap)
}

data class TieredInspectionOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the inspection override steps in order and keeps a trace of each. */
data class InspectionOverrideEngine(
    var steps: MutableList<InspectionOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionOverride): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionOverrideEngine(flags: Registry): InspectionOverrideEngine {
    return InspectionOverrideEngine(
        steps = mutableListOf<InspectionOverrideStep>(
            NameInspectionOverrideAdjuster(mutableMapOf<String, Double>("west" to 1.22, "north" to 1.36)),
            FlaggedInspectionOverrideAdjuster(flags, "inspection-override-manual", -2.7),
            RoundingInspectionOverrideAdjuster(0.25),
            CapInspectionOverrideAdjuster(19839.0),
            TieredInspectionOverrideAdjuster(
                mutableListOf<Double>(2867.0, 3967.0, 6625.0),
                mutableListOf<Double>(1.03, 1.08, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 80994.4,
    )
}
