package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the audit bundle calculation. */
interface AuditBundleFactor {
    fun describe(): String
    fun adjust(item: AuditBundle, value: Double): Double
}

data class PercentAuditBundleAdjuster(var percent: Double) : AuditBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AuditBundleAmountOffsetAdjuster(var weight: Double) : AuditBundleFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: AuditBundle, value: Double): Double = value + item.amount * weight
}

data class FlaggedAuditBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the audit bundle steps in order and keeps a trace of each. */
data class AuditBundleEngine(
    var steps: MutableList<AuditBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditBundle): Double {
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

    fun evaluateAll(items: MutableList<AuditBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditBundleEngine(flags: Registry): AuditBundleEngine {
    return AuditBundleEngine(
        steps = mutableListOf<AuditBundleFactor>(
            PercentAuditBundleAdjuster(-13.1),
            AuditBundleAmountOffsetAdjuster(0.71),
            FlaggedAuditBundleAdjuster(flags, "audit-bundle-capped", -2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 89296.9,
    )
}
