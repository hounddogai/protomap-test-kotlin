package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the primary defect bundle calculation. */
interface PrimaryDefectBundleModifier {
    fun describe(): String
    fun adjust(item: PrimaryDefectBundle, value: Double): Double
}

data class CapPrimaryDefectBundleAdjuster(var cap: Double) : PrimaryDefectBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDefectBundle, value: Double): Double = minOf(value, cap)
}

data class TitlePrimaryDefectBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDefectBundleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimaryDefectBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPrimaryDefectBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryDefectBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryDefectBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryDefectBundleRevisionOffsetAdjuster(var weight: Double) : PrimaryDefectBundleModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: PrimaryDefectBundle, value: Double): Double = value + item.revision.toDouble() * weight
}

data class FlaggedPrimaryDefectBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDefectBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDefectBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary defect bundle steps in order and keeps a trace of each. */
data class PrimaryDefectBundleEngine(
    var steps: MutableList<PrimaryDefectBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryDefectBundle): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDefectBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDefectBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryDefectBundleEngine(flags: Registry): PrimaryDefectBundleEngine {
    return PrimaryDefectBundleEngine(
        steps = mutableListOf<PrimaryDefectBundleModifier>(
            CapPrimaryDefectBundleAdjuster(35395.0),
            TitlePrimaryDefectBundleAdjuster(mutableMapOf<String, Double>("north" to 0.8, "west" to 0.88)),
            TieredPrimaryDefectBundleAdjuster(
                mutableListOf<Double>(3554.0, 5809.0, 8670.0),
                mutableListOf<Double>(0.97, 0.97, 0.92),
            ),
            PrimaryDefectBundleRevisionOffsetAdjuster(1.96),
            FlaggedPrimaryDefectBundleAdjuster(flags, "primary-defect-bundle-strict", 0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 57800.3,
    )
}
