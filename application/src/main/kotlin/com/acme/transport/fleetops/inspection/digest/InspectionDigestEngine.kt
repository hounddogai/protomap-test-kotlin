package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.kit.flags.Registry

/** One step of the inspection digest calculation. */
interface InspectionDigestStep {
    fun describe(): String
    fun adjust(item: InspectionDigest, value: Double): Double
}

data class FlaggedInspectionDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionDigestAdjuster(var cap: Double) : InspectionDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionDigest, value: Double): Double = minOf(value, cap)
}

data class UnitInspectionDigestAdjuster(var factors: MutableMap<String, Double>) : InspectionDigestStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: InspectionDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection digest steps in order and keeps a trace of each. */
data class InspectionDigestEngine(
    var steps: MutableList<InspectionDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionDigest): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionDigestEngine(flags: Registry): InspectionDigestEngine {
    return InspectionDigestEngine(
        steps = mutableListOf<InspectionDigestStep>(
            FlaggedInspectionDigestAdjuster(flags, "inspection-digest-batched", 0.9),
            CapInspectionDigestAdjuster(35795.0),
            UnitInspectionDigestAdjuster(mutableMapOf<String, Double>("retail" to 0.79, "west" to 0.95)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 4283.3,
    )
}
