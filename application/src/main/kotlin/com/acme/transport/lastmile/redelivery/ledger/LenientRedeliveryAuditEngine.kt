package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient redelivery audit calculation. */
interface LenientRedeliveryAuditStep {
    fun describe(): String
    fun adjust(item: LenientRedeliveryAudit, value: Double): Double
}

data class LenientRedeliveryAuditMinimumOffsetAdjuster(var weight: Double) : LenientRedeliveryAuditStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: LenientRedeliveryAudit, value: Double): Double = value + item.minimum * weight
}

data class MinimumLenientRedeliveryAuditAdjuster(var minimum: Double) : LenientRedeliveryAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRedeliveryAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelLenientRedeliveryAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientRedeliveryAuditStep {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientRedeliveryAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientRedeliveryAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientRedeliveryAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientRedeliveryAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientRedeliveryAuditAdjuster(var percent: Double) : LenientRedeliveryAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRedeliveryAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientRedeliveryAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientRedeliveryAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientRedeliveryAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient redelivery audit steps in order and keeps a trace of each. */
data class LenientRedeliveryAuditEngine(
    var steps: MutableList<LenientRedeliveryAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientRedeliveryAudit): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientRedeliveryAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientRedeliveryAuditEngine(flags: Registry): LenientRedeliveryAuditEngine {
    return LenientRedeliveryAuditEngine(
        steps = mutableListOf<LenientRedeliveryAuditStep>(
            LenientRedeliveryAuditMinimumOffsetAdjuster(1.2),
            MinimumLenientRedeliveryAuditAdjuster(16.3),
            LabelLenientRedeliveryAuditAdjuster(
                mutableMapOf<String, Double>("north" to 1.11, "west" to 0.82, "online" to 0.83),
            ),
            FlaggedLenientRedeliveryAuditAdjuster(flags, "lenient-redelivery-audit-strict", -8.8),
            PercentLenientRedeliveryAuditAdjuster(-1.4),
            TieredLenientRedeliveryAuditAdjuster(
                mutableListOf<Double>(530.0, 8168.0),
                mutableListOf<Double>(0.91, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 55458.6,
    )
}
