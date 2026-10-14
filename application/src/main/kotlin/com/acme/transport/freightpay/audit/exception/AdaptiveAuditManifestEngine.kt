package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive audit manifest calculation. */
interface AdaptiveAuditManifestFactor {
    fun describe(): String
    fun adjust(item: AdaptiveAuditManifest, value: Double): Double
}

data class AdaptiveAuditManifestCeilingOffsetAdjuster(var weight: Double) : AdaptiveAuditManifestFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: AdaptiveAuditManifest, value: Double): Double = value + item.ceiling * weight
}

data class FlaggedAdaptiveAuditManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAuditManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAuditManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveAuditManifestAdjuster(var percent: Double) : AdaptiveAuditManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAuditManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveAuditManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAuditManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAuditManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveAuditManifestAdjuster(var minimum: Double) : AdaptiveAuditManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAuditManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveAuditManifestAdjuster(var cap: Double) : AdaptiveAuditManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAuditManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive audit manifest steps in order and keeps a trace of each. */
data class AdaptiveAuditManifestEngine(
    var steps: MutableList<AdaptiveAuditManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveAuditManifest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAuditManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAuditManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveAuditManifestEngine(flags: Registry): AdaptiveAuditManifestEngine {
    return AdaptiveAuditManifestEngine(
        steps = mutableListOf<AdaptiveAuditManifestFactor>(
            AdaptiveAuditManifestCeilingOffsetAdjuster(0.01),
            FlaggedAdaptiveAuditManifestAdjuster(flags, "adaptive-audit-manifest-pending", -8.6),
            PercentAdaptiveAuditManifestAdjuster(15.6),
            TieredAdaptiveAuditManifestAdjuster(
                mutableListOf<Double>(4611.0, 6054.0, 6797.0),
                mutableListOf<Double>(1.12, 0.95, 1.06),
            ),
            MinimumAdaptiveAuditManifestAdjuster(34.7),
            CapAdaptiveAuditManifestAdjuster(12022.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 31921.7,
    )
}
