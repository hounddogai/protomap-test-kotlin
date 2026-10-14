package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.flags.Registry

/** One step of the global qualification bundle calculation. */
interface GlobalQualificationBundleModifier {
    fun describe(): String
    fun adjust(item: GlobalQualificationBundle, value: Double): Double
}

data class GlobalQualificationBundleBatchSizeOffsetAdjuster(
    var weight: Double,
) : GlobalQualificationBundleModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: GlobalQualificationBundle, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class FlaggedGlobalQualificationBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalQualificationBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalQualificationBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalQualificationBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalQualificationBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalQualificationBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global qualification bundle steps in order and keeps a trace of each. */
data class GlobalQualificationBundleEngine(
    var steps: MutableList<GlobalQualificationBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalQualificationBundle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalQualificationBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalQualificationBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalQualificationBundleEngine(flags: Registry): GlobalQualificationBundleEngine {
    return GlobalQualificationBundleEngine(
        steps = mutableListOf<GlobalQualificationBundleModifier>(
            GlobalQualificationBundleBatchSizeOffsetAdjuster(1.13),
            FlaggedGlobalQualificationBundleAdjuster(flags, "global-qualification-bundle-primary", 0.9),
            TieredGlobalQualificationBundleAdjuster(
                mutableListOf<Double>(35.0, 55.0, 503.0, 6577.0),
                mutableListOf<Double>(1.02, 0.83, 1.18, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 27974.5,
    )
}
