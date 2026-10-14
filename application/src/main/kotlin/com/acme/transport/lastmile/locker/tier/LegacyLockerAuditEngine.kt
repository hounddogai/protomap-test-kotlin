package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy locker audit calculation. */
interface LegacyLockerAuditModifier {
    fun describe(): String
    fun adjust(item: LegacyLockerAudit, value: Double): Double
}

data class TieredLegacyLockerAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyLockerAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyLockerAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyLockerAuditAdjuster(var step: Double) : LegacyLockerAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyLockerAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyLockerAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLockerAuditModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLockerAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyLockerAuditAdjuster(var cap: Double) : LegacyLockerAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLockerAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy locker audit steps in order and keeps a trace of each. */
data class LegacyLockerAuditEngine(
    var steps: MutableList<LegacyLockerAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyLockerAudit): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyLockerAuditModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyLockerAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyLockerAuditEngine(flags: Registry): LegacyLockerAuditEngine {
    return LegacyLockerAuditEngine(
        steps = mutableListOf<LegacyLockerAuditModifier>(
            TieredLegacyLockerAuditAdjuster(
                mutableListOf<Double>(175.0, 4920.0, 6819.0, 9362.0),
                mutableListOf<Double>(1.16, 0.86, 0.98, 0.85),
            ),
            RoundingLegacyLockerAuditAdjuster(0.25),
            FlaggedLegacyLockerAuditAdjuster(flags, "legacy-locker-audit-batched", 9.3),
            CapLegacyLockerAuditAdjuster(36384.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 42986.0,
    )
}
