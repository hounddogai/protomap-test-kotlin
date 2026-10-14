package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy audit manifest calculation. */
interface LegacyAuditManifestAdjuster {
    fun describe(): String
    fun adjust(item: LegacyAuditManifest, value: Double): Double
}

data class LegacyAuditManifestMaximumOffsetAdjuster(var weight: Double) : LegacyAuditManifestAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: LegacyAuditManifest, value: Double): Double = value + item.maximum * weight
}

data class CapLegacyAuditManifestAdjuster(var cap: Double) : LegacyAuditManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyAuditManifest, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyAuditManifestAdjuster(var minimum: Double) : LegacyAuditManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyAuditManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyAuditManifestAdjuster(var percent: Double) : LegacyAuditManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyAuditManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyAuditManifestAdjuster(var step: Double) : LegacyAuditManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyAuditManifest, value: Double): Double = roundTo(value, step)
}

data class LabelLegacyAuditManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyAuditManifestAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyAuditManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy audit manifest steps in order and keeps a trace of each. */
data class LegacyAuditManifestEngine(
    var steps: MutableList<LegacyAuditManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyAuditManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyAuditManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyAuditManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyAuditManifestEngine(): LegacyAuditManifestEngine {
    return LegacyAuditManifestEngine(
        steps = mutableListOf<LegacyAuditManifestAdjuster>(
            LegacyAuditManifestMaximumOffsetAdjuster(1.45),
            CapLegacyAuditManifestAdjuster(9102.0),
            MinimumLegacyAuditManifestAdjuster(42.4),
            PercentLegacyAuditManifestAdjuster(-22.3),
            RoundingLegacyAuditManifestAdjuster(0.25),
            LabelLegacyAuditManifestAdjuster(mutableMapOf<String, Double>("retail" to 1.23)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 22793.3,
    )
}
