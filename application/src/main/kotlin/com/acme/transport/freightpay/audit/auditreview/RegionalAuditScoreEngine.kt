package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional audit score calculation. */
interface RegionalAuditScoreModifier {
    fun describe(): String
    fun adjust(item: RegionalAuditScore, value: Double): Double
}

data class SegmentRegionalAuditScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalAuditScoreModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: RegionalAuditScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalAuditScoreAdjuster(var minimum: Double) : RegionalAuditScoreModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalAuditScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalAuditScoreQuantityOffsetAdjuster(var weight: Double) : RegionalAuditScoreModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RegionalAuditScore, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class FlaggedRegionalAuditScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalAuditScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalAuditScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalAuditScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalAuditScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalAuditScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalAuditScoreAdjuster(var cap: Double) : RegionalAuditScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalAuditScore, value: Double): Double = minOf(value, cap)
}

/** Runs the regional audit score steps in order and keeps a trace of each. */
data class RegionalAuditScoreEngine(
    var steps: MutableList<RegionalAuditScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalAuditScore): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalAuditScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalAuditScoreEngine(flags: Registry): RegionalAuditScoreEngine {
    return RegionalAuditScoreEngine(
        steps = mutableListOf<RegionalAuditScoreModifier>(
            SegmentRegionalAuditScoreAdjuster(
                mutableMapOf<String, Double>("retail" to 1.38, "north" to 1.11, "east" to 1.31),
            ),
            MinimumRegionalAuditScoreAdjuster(15.2),
            RegionalAuditScoreQuantityOffsetAdjuster(1.4),
            FlaggedRegionalAuditScoreAdjuster(flags, "regional-audit-score-legacy", -3.9),
            TieredRegionalAuditScoreAdjuster(
                mutableListOf<Double>(1733.0, 8889.0, 9094.0, 9427.0),
                mutableListOf<Double>(0.91, 1.18, 0.97, 0.81),
            ),
            CapRegionalAuditScoreAdjuster(13846.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 46929.0,
    )
}
