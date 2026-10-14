package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered audit manifest calculation. */
interface TieredAuditManifestModifier {
    fun describe(): String
    fun adjust(item: TieredAuditManifest, value: Double): Double
}

data class FlaggedTieredAuditManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredAuditManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredAuditManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredAuditManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredAuditManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredAuditManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredAuditManifestAdjuster(var step: Double) : TieredAuditManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredAuditManifest, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredAuditManifestAdjuster(var minimum: Double) : TieredAuditManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredAuditManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAuditManifestWeightOffsetAdjuster(var weight: Double) : TieredAuditManifestModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: TieredAuditManifest, value: Double): Double = value + item.weight * weight
}

data class PercentTieredAuditManifestAdjuster(var percent: Double) : TieredAuditManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredAuditManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered audit manifest steps in order and keeps a trace of each. */
data class TieredAuditManifestEngine(
    var steps: MutableList<TieredAuditManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredAuditManifest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredAuditManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredAuditManifestEngine(flags: Registry): TieredAuditManifestEngine {
    return TieredAuditManifestEngine(
        steps = mutableListOf<TieredAuditManifestModifier>(
            FlaggedTieredAuditManifestAdjuster(flags, "tiered-audit-manifest-tiered", -3.9),
            TieredTieredAuditManifestAdjuster(
                mutableListOf<Double>(1171.0, 4030.0, 8602.0, 9665.0),
                mutableListOf<Double>(0.86, 0.88, 1.15, 0.99),
            ),
            RoundingTieredAuditManifestAdjuster(0.05),
            MinimumTieredAuditManifestAdjuster(28.2),
            TieredAuditManifestWeightOffsetAdjuster(0.33),
            PercentTieredAuditManifestAdjuster(21.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 10229.7,
    )
}
