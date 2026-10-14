package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive dispute manifest calculation. */
interface AdaptiveDisputeManifestModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDisputeManifest, value: Double): Double
}

data class CapAdaptiveDisputeManifestAdjuster(var cap: Double) : AdaptiveDisputeManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDisputeManifest, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveDisputeManifestAdjuster(var percent: Double) : AdaptiveDisputeManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDisputeManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveDisputeManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDisputeManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDisputeManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveDisputeManifestAdjuster(var step: Double) : AdaptiveDisputeManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDisputeManifest, value: Double): Double = roundTo(value, step)
}

data class NameAdaptiveDisputeManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveDisputeManifestModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveDisputeManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive dispute manifest steps in order and keeps a trace of each. */
data class AdaptiveDisputeManifestEngine(
    var steps: MutableList<AdaptiveDisputeManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDisputeManifest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDisputeManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDisputeManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDisputeManifestEngine(flags: Registry): AdaptiveDisputeManifestEngine {
    return AdaptiveDisputeManifestEngine(
        steps = mutableListOf<AdaptiveDisputeManifestModifier>(
            CapAdaptiveDisputeManifestAdjuster(21296.0),
            PercentAdaptiveDisputeManifestAdjuster(-3.7),
            FlaggedAdaptiveDisputeManifestAdjuster(flags, "adaptive-dispute-manifest-weighted", 2.8),
            RoundingAdaptiveDisputeManifestAdjuster(0.05),
            NameAdaptiveDisputeManifestAdjuster(
                mutableMapOf<String, Double>("retail" to 1.15, "central" to 1.04, "east" to 1.35),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 24557.6,
    )
}
