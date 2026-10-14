package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual trailer variance calculation. */
interface ManualTrailerVarianceModifier {
    fun describe(): String
    fun adjust(item: ManualTrailerVariance, value: Double): Double
}

data class NameManualTrailerVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTrailerVarianceModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualTrailerVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualTrailerVarianceAdjuster(var cap: Double) : ManualTrailerVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerVariance, value: Double): Double = minOf(value, cap)
}

data class PercentManualTrailerVarianceAdjuster(var percent: Double) : ManualTrailerVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualTrailerVarianceAdjuster(var step: Double) : ManualTrailerVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTrailerVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedManualTrailerVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTrailerVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTrailerVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualTrailerVarianceAdjuster(var minimum: Double) : ManualTrailerVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTrailerVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual trailer variance steps in order and keeps a trace of each. */
data class ManualTrailerVarianceEngine(
    var steps: MutableList<ManualTrailerVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualTrailerVariance): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTrailerVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTrailerVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualTrailerVarianceEngine(flags: Registry): ManualTrailerVarianceEngine {
    return ManualTrailerVarianceEngine(
        steps = mutableListOf<ManualTrailerVarianceModifier>(
            NameManualTrailerVarianceAdjuster(
                mutableMapOf<String, Double>("online" to 0.95, "north" to 1.35, "central" to 1.16),
            ),
            CapManualTrailerVarianceAdjuster(21328.0),
            PercentManualTrailerVarianceAdjuster(16.3),
            RoundingManualTrailerVarianceAdjuster(0.05),
            FlaggedManualTrailerVarianceAdjuster(flags, "manual-trailer-variance-pending", -6.1),
            MinimumManualTrailerVarianceAdjuster(43.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 52939.7,
    )
}
