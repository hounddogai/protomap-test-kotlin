package com.acme.transport.freightpay.audit.draft

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered audit digest calculation. */
interface TieredAuditDigestStep {
    fun describe(): String
    fun adjust(item: TieredAuditDigest, value: Double): Double
}

data class MinimumTieredAuditDigestAdjuster(var minimum: Double) : TieredAuditDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredAuditDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredAuditDigestAdjuster(var percent: Double) : TieredAuditDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredAuditDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAuditDigestGraceDaysOffsetAdjuster(var weight: Double) : TieredAuditDigestStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: TieredAuditDigest, value: Double): Double = value + item.graceDays.toDouble() * weight
}

/** Runs the tiered audit digest steps in order and keeps a trace of each. */
data class TieredAuditDigestEngine(
    var steps: MutableList<TieredAuditDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredAuditDigest): Double {
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

    fun evaluateAll(items: MutableList<TieredAuditDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredAuditDigestEngine(): TieredAuditDigestEngine {
    return TieredAuditDigestEngine(
        steps = mutableListOf<TieredAuditDigestStep>(
            MinimumTieredAuditDigestAdjuster(48.7),
            PercentTieredAuditDigestAdjuster(-15.8),
            TieredAuditDigestGraceDaysOffsetAdjuster(0.31),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 29593.2,
    )
}
