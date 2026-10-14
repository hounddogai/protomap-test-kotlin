package com.acme.transport.freightpay.audit.auditoverride

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global audit bundle calculation. */
interface GlobalAuditBundleModifier {
    fun describe(): String
    fun adjust(item: GlobalAuditBundle, value: Double): Double
}

data class CapGlobalAuditBundleAdjuster(var cap: Double) : GlobalAuditBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAuditBundle, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalAuditBundleAdjuster(var step: Double) : GlobalAuditBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAuditBundle, value: Double): Double = roundTo(value, step)
}

data class TitleGlobalAuditBundleAdjuster(var factors: MutableMap<String, Double>) : GlobalAuditBundleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalAuditBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalAuditBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAuditBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAuditBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalAuditBundleWindowDaysOffsetAdjuster(var weight: Double) : GlobalAuditBundleModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: GlobalAuditBundle, value: Double): Double = value + item.windowDays.toDouble() * weight
}

/** Runs the global audit bundle steps in order and keeps a trace of each. */
data class GlobalAuditBundleEngine(
    var steps: MutableList<GlobalAuditBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalAuditBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalAuditBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalAuditBundleEngine(flags: Registry): GlobalAuditBundleEngine {
    return GlobalAuditBundleEngine(
        steps = mutableListOf<GlobalAuditBundleModifier>(
            CapGlobalAuditBundleAdjuster(48695.0),
            RoundingGlobalAuditBundleAdjuster(5.0),
            TitleGlobalAuditBundleAdjuster(
                mutableMapOf<String, Double>("east" to 1.14, "retail" to 0.93, "west" to 1.06),
            ),
            FlaggedGlobalAuditBundleAdjuster(flags, "global-audit-bundle-rolling", 0.7),
            GlobalAuditBundleWindowDaysOffsetAdjuster(1.96),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 52822.8,
    )
}
