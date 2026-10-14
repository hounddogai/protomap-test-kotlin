package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual fuel burn digest calculation. */
interface ManualFuelBurnDigestModifier {
    fun describe(): String
    fun adjust(item: ManualFuelBurnDigest, value: Double): Double
}

data class FlaggedManualFuelBurnDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualFuelBurnDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualFuelBurnDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleManualFuelBurnDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualFuelBurnDigestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualFuelBurnDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualFuelBurnDigestVersionOffsetAdjuster(var weight: Double) : ManualFuelBurnDigestModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ManualFuelBurnDigest, value: Double): Double = value + item.version.toDouble() * weight
}

data class MinimumManualFuelBurnDigestAdjuster(var minimum: Double) : ManualFuelBurnDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualFuelBurnDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualFuelBurnDigestAdjuster(var percent: Double) : ManualFuelBurnDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualFuelBurnDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualFuelBurnDigestAdjuster(var step: Double) : ManualFuelBurnDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualFuelBurnDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the manual fuel burn digest steps in order and keeps a trace of each. */
data class ManualFuelBurnDigestEngine(
    var steps: MutableList<ManualFuelBurnDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualFuelBurnDigest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualFuelBurnDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualFuelBurnDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualFuelBurnDigestEngine(flags: Registry): ManualFuelBurnDigestEngine {
    return ManualFuelBurnDigestEngine(
        steps = mutableListOf<ManualFuelBurnDigestModifier>(
            FlaggedManualFuelBurnDigestAdjuster(flags, "manual-fuel-burn-digest-global", 7.1),
            TitleManualFuelBurnDigestAdjuster(mutableMapOf<String, Double>("central" to 0.76)),
            ManualFuelBurnDigestVersionOffsetAdjuster(0.53),
            MinimumManualFuelBurnDigestAdjuster(37.8),
            PercentManualFuelBurnDigestAdjuster(6.9),
            RoundingManualFuelBurnDigestAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 15480.9,
    )
}
