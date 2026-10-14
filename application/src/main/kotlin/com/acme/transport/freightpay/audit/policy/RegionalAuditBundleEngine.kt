package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional audit bundle calculation. */
interface RegionalAuditBundleModifier {
    fun describe(): String
    fun adjust(item: RegionalAuditBundle, value: Double): Double
}

data class PercentRegionalAuditBundleAdjuster(var percent: Double) : RegionalAuditBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalAuditBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalAuditBundleDiscountOffsetAdjuster(var weight: Double) : RegionalAuditBundleModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: RegionalAuditBundle, value: Double): Double = value + item.discount * weight
}

data class CapRegionalAuditBundleAdjuster(var cap: Double) : RegionalAuditBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalAuditBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalAuditBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalAuditBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalAuditBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleRegionalAuditBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalAuditBundleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalAuditBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalAuditBundleAdjuster(var minimum: Double) : RegionalAuditBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalAuditBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional audit bundle steps in order and keeps a trace of each. */
data class RegionalAuditBundleEngine(
    var steps: MutableList<RegionalAuditBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalAuditBundle): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalAuditBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalAuditBundleEngine(flags: Registry): RegionalAuditBundleEngine {
    return RegionalAuditBundleEngine(
        steps = mutableListOf<RegionalAuditBundleModifier>(
            PercentRegionalAuditBundleAdjuster(22.0),
            RegionalAuditBundleDiscountOffsetAdjuster(0.39),
            CapRegionalAuditBundleAdjuster(16171.0),
            FlaggedRegionalAuditBundleAdjuster(flags, "regional-audit-bundle-capped", -3.1),
            TitleRegionalAuditBundleAdjuster(mutableMapOf<String, Double>("online" to 1.14, "north" to 1.2)),
            MinimumRegionalAuditBundleAdjuster(9.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 37971.8,
    )
}
