package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.flags.Registry

/** One step of the pending qualification preference calculation. */
interface PendingQualificationPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: PendingQualificationPreference, value: Double): Double
}

data class PercentPendingQualificationPreferenceAdjuster(
    var percent: Double,
) : PendingQualificationPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingQualificationPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingQualificationPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingQualificationPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingQualificationPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingQualificationPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingQualificationPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingQualificationPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending qualification preference steps in order and keeps a trace of each. */
data class PendingQualificationPreferenceEngine(
    var steps: MutableList<PendingQualificationPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingQualificationPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingQualificationPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingQualificationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingQualificationPreferenceEngine(flags: Registry): PendingQualificationPreferenceEngine {
    return PendingQualificationPreferenceEngine(
        steps = mutableListOf<PendingQualificationPreferenceAdjuster>(
            PercentPendingQualificationPreferenceAdjuster(6.8),
            FlaggedPendingQualificationPreferenceAdjuster(
                flags,
                "pending-qualification-preference-seasonal",
                4.2,
            ),
            TieredPendingQualificationPreferenceAdjuster(
                mutableListOf<Double>(5226.0, 8568.0, 9581.0),
                mutableListOf<Double>(1.0, 0.9, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 57100.4,
    )
}
