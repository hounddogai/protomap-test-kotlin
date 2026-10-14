package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the clearance quota calculation. */
interface ClearanceQuotaModifier {
    fun describe(): String
    fun adjust(item: ClearanceQuota, value: Double): Double
}

data class ClearanceQuotaPalletsOffsetAdjuster(var weight: Double) : ClearanceQuotaModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ClearanceQuota, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class FlaggedClearanceQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentClearanceQuotaAdjuster(var percent: Double) : ClearanceQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleClearanceQuotaAdjuster(var factors: MutableMap<String, Double>) : ClearanceQuotaModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ClearanceQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapClearanceQuotaAdjuster(var cap: Double) : ClearanceQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the clearance quota steps in order and keeps a trace of each. */
data class ClearanceQuotaEngine(
    var steps: MutableList<ClearanceQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ClearanceQuota): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultClearanceQuotaEngine(flags: Registry): ClearanceQuotaEngine {
    return ClearanceQuotaEngine(
        steps = mutableListOf<ClearanceQuotaModifier>(
            ClearanceQuotaPalletsOffsetAdjuster(1.26),
            FlaggedClearanceQuotaAdjuster(flags, "clearance-quota-capped", 2.0),
            PercentClearanceQuotaAdjuster(6.8),
            TitleClearanceQuotaAdjuster(mutableMapOf<String, Double>("south" to 1.24)),
            CapClearanceQuotaAdjuster(19457.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 98245.0,
    )
}
